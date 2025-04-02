package pl.pw.geogame

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.pw.geogame.data.model.loadBeaconsFromAssets
import pl.pw.geogame.data.model.pois


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "pw.MainActivity"
    }
    private var MIN_DISTANCE_TO_POI = 10.0


    private var canScan = false
    private var popupVisible = false

    private lateinit var connectionStateReceiver: ConnectionStateChangeReceiver
    private var beaconManager: BeaconManager? = null
    private val region = Region("all-beacons-region", null, null, null)

    private var userMarker: Marker? = null
    private val map: MapView by lazy {
        findViewById(R.id.map_view)
    }
    private lateinit var scanButton: Button

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.any { !it.value }) {
                Toast.makeText(
                    this,
                    "Bez uprawnień aplikacja nie będzie działać prawidłowo...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                listenForConnectionChanges()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        loadReferenceBeaconData()
        setUpUI()
        setUpBeaconManager()
        requestRequiredPermissions()

        showEmptyMap()
        drawPOIs()

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
    }

    private fun loadReferenceBeaconData() {
        val beacons = loadBeaconsFromAssets(assets)
        Log.d(TAG, "Wczytano dane dla ${beacons.size} beaconów")
    }

    private fun requestRequiredPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
        if (allPermissionsGranted(permissions)) {
            listenForConnectionChanges()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun allPermissionsGranted(
        permissions: Array<String>,
    ): Boolean {
        permissions.forEach { permissionName ->
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    permissionName
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    private fun listenForConnectionChanges() {
        Toast.makeText(
            this,
            "Upewnij się, że masz włączony Bluetooth i Lokalizację.",
            Toast.LENGTH_SHORT
        ).show()

        connectionStateReceiver = ConnectionStateChangeReceiver(
            context = this,
            onBothEnabled = {
                canScan = true
                runOnUiThread { scanButton.isEnabled = true }
            },
            onEitherDisabled = {
                canScan = false
                runOnUiThread { scanButton.isEnabled = false }
                cleanupBeaconManager()
            }
        )

        val intentFilter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        registerReceiver(connectionStateReceiver, intentFilter)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothEnabled = bluetoothManager.adapter.isEnabled
        if (gpsEnabled && bluetoothEnabled) {
            canScan = true
        }
    }

    private fun scanForBeaconsPossible() {
        scanBeacons()
    }

//    private fun startRangingBeacons(){
//        if (beaconManager == null) {
//            setUpBeaconManager()
//        }
//        else {
//            scanBeacons()
//        }
//    }

    private fun setUpBeaconManager() {
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(this)

            listOf(
                BeaconParser.EDDYSTONE_UID_LAYOUT,
                BeaconParser.EDDYSTONE_TLM_LAYOUT,
                BeaconParser.EDDYSTONE_URL_LAYOUT,
            ).forEach {
                beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(it))
            }

            // Scan config
            beaconManager?.foregroundScanPeriod = 1100L
            beaconManager?.foregroundBetweenScanPeriod = 1000L
            beaconManager?.updateScanPeriods()

            // Logger
            beaconManager?.addRangeNotifier { beacons, _ ->
                Log.d(TAG, "Znaleziono ${beacons.size} beaconów")
                beacons.forEach {
                    Log.d(TAG, "Beacon: ${it.bluetoothAddress}, odl: ${it.distance} m")
                }
                if (beacons.size < 3) {
                    Log.d(TAG, "Nie ma wystarczająco beaconów na trilaterację (${beacons.size})")
                    simulateBeaconScan()
                }
                else {
                    calculateDevicePosition(beacons)
                }
            }
        }
    }

    private fun cleanupBeaconManager() {
        beaconManager?.stopRangingBeacons(region)
    }

    private fun scanBeacons() {
        Log.d(TAG,"Skanowanie beaconów...")
        beaconManager?.startRangingBeacons(region)
    }

    private fun simulateBeaconScan() {
        val allBeacons = loadBeaconsFromAssets(assets)
        if (allBeacons.isEmpty()) {
            Log.w(TAG, "W assets nie ma beaconów.")
            return
        }
        Log.d(TAG, "Symulowanie skanu beaconów...")

        // Pick 3–6 random beacons
        val randomBeacons = allBeacons.shuffled().take((3..6).random())

        val fakeBeacons = randomBeacons.map { beacon ->
            val distanceMeters = (1..5).random().toDouble() + Math.random()
            val txPower = -59
            val rssi = txPower - (10 * 2 * Math.log10(distanceMeters))

            Beacon.Builder()
                .setBluetoothAddress(beacon.beaconUid)
                .setTxPower(txPower)
                .setRssi(rssi.toInt())
                .build()
        }
        for (bc in fakeBeacons) {
            Log.d(TAG, "Symulowany beacon: ${bc.bluetoothAddress}, odl: ${bc.distance}")
        }

        calculateDevicePosition(fakeBeacons)
    }

    private fun calculateDevicePosition(scannedBeacons: Collection<Beacon>) {
        val referenceBeacons = loadBeaconsFromAssets(assets)
        val matched = scannedBeacons.mapNotNull { beacon ->
            val uid = beacon.bluetoothAddress.toString().uppercase()
            val ref = referenceBeacons.find { it.beaconUid?.uppercase() == uid }
            if (ref != null) {
                Triple(ref.latitude, ref.longitude, beacon.distance)
            } else null
        }

        // Prosty ważony środek geometryczny odwrotnie proporcjonalny do odległości
        val weights = matched.map { 1.0 / it.third }
        val weightSum = weights.sum()
        val lat = matched.zip(weights).sumOf { it.first.first * it.second } / weightSum
        val lon = matched.zip(weights).sumOf { it.first.second * it.second } / weightSum

        Log.d(TAG, "Lokalizacja użytkownika: ($lat, $lon)")

        showUserOnMap(lat, lon)
        checkProximityToPOI(lat, lon)
    }

    private fun setUpUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scanButton = findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            if (canScan) {
                Log.d(TAG, "Wywołano skanowanie")
                scanForBeaconsPossible()
            } else {
                Toast.makeText(this, "Włącz Bluetooth i lokalizację żeby zacząć skanowanie!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserOnMap(lat: Double, lon: Double) {
        val drawable = ContextCompat.getDrawable(this, R.drawable.location)
        if (drawable == null) {
            Log.e(TAG, "Nie znaleziono ikony lokalizacji")
            return
        }

        var width = 75
        var height = 75

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        val mapController = map.controller
        mapController.setZoom(20.0)
        val pos= GeoPoint(lat, lon)
        mapController.setCenter(pos)

        if (userMarker == null) {
            userMarker = Marker(map).apply {
                position = pos
                icon = BitmapDrawable(resources, bitmap)
                title = "Jesteś tutaj"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            map.overlays.add(userMarker)
        } else {
            userMarker?.position = GeoPoint(lat, lon)
        }

        map.invalidate()
    }

    private fun showEmptyMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        val userLocationAvailable = userMarker != null
        val userGeoPoint = userMarker?.position ?: GeoPoint(52.220605482382545, 21.010149484476507)

        if (userLocationAvailable) {
            mapController.setZoom(20.0)
            mapController.setCenter(userGeoPoint)
        } else {
            mapController.setZoom(19.0)
            mapController.setCenter(userGeoPoint)
        }
    }

    private fun drawPOIs() {
        val drawable = ContextCompat.getDrawable(this, R.drawable.poi)
        if (drawable == null) {
            Log.e(TAG, "Nie znaleziono ikony lokalizacji")
            return
        }

        var width = 50
        var height = 50

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)


        pois.forEach { obj ->
            val marker = Marker(map).apply {
                position = GeoPoint(obj.latitude, obj.longitude)
                icon = BitmapDrawable(resources, bitmap)
                title = obj.name
//                snippet = obj.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun showPOI(title: String, description: String, onClose: () -> Unit) {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_poi, null)

        view.findViewById<TextView>(R.id.dialog_title).text = title
        view.findViewById<TextView>(R.id.dialog_description).text = description

        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation // dodaj to!

        view.findViewById<Button>(R.id.dialog_ok).setOnClickListener {
            dialog.dismiss()
            onClose()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun checkProximityToPOI(userLat: Double, userLon: Double) {
        if (popupVisible) return
        val userLocation = android.location.Location("").apply {
            latitude = userLat
            longitude = userLon
        }

        pois.forEach { obj ->
            val objLocation = android.location.Location("").apply {
                latitude = obj.latitude
                longitude = obj.longitude
            }

            val distance = userLocation.distanceTo(objLocation)

            if (distance <= MIN_DISTANCE_TO_POI) {
                popupVisible = true
                cleanupBeaconManager()

                showPOI(obj.name, obj.description) {
                    popupVisible = false
                    scanForBeaconsPossible()
                }

                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectionStateReceiver)
        Log.d(TAG, "onDestroy")

    }

}
