package pl.pw.geogame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.util.Log

class ConnectionStateChangeReceiver(
    context: Context,
    private val onBothEnabled: () -> Unit,
    private val onEitherDisabled: () -> Unit
) : BroadcastReceiver() {
    var isGpsEnabled = false
    var isBluetoothEnabled = false

    init {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true

        scanBLEDevices()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action == null) return

        when (intent.action) {
            LocationManager.PROVIDERS_CHANGED_ACTION -> {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                scanBLEDevices()
                Log.d("Connection Receiver", "GPS enabled: $isGpsEnabled")
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                isBluetoothEnabled = state == BluetoothAdapter.STATE_ON
                scanBLEDevices()
                Log.d("ConnectionReceiver", "Bluetooth Enabled: $isBluetoothEnabled")
            }
        }
    }

    private fun scanBLEDevices() {
        if (isGpsEnabled && isBluetoothEnabled) {
            onBothEnabled()
        } else {
            onEitherDisabled()
        }
    }
}
