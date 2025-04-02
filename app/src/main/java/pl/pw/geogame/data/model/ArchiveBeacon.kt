package pl.pw.geogame.data.model

import android.content.res.AssetManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BeaconResponse(
    val items: List<ArchiveBeacon>
)

data class ArchiveBeacon(
    val longitude: Double,
    val latitude: Double,
    val beaconUid: String?,
//    val floor: String = "Nieznany"
)

fun mapFloorIdToName(floorId: Int): String {
    return when (floorId) {
        51 -> "Parter"
        52 -> "Piętro 1"
        53 -> "Piętro 2"
        54 -> "Piętro 3"
        55 -> "Piętro 3"
        56 -> "Piętro 4"
        99 -> "Na zewnątrz"
        else -> "Nieznany"
    }
}

fun loadBeaconsFromAssets(assetManager: AssetManager): List<ArchiveBeacon> {
    val gson = Gson()
    val type = object : TypeToken<BeaconResponse>() {}.type

    val txtFiles = assetManager.list("")?.filter { it.endsWith(".txt") } ?: emptyList()
    val allBeacons = mutableListOf<ArchiveBeacon>()

    for (fileName in txtFiles) {
        try {
            val inputStream = assetManager.open(fileName)
            val json = inputStream.bufferedReader().use { it.readText() }
            val response: BeaconResponse = gson.fromJson(json, type)
            val beacons = response.items
                .filter { it.beaconUid != null }
                .map {
                    ArchiveBeacon(it.longitude, it.latitude, it.beaconUid!!)
                }
            allBeacons.addAll(beacons)
        } catch (e: Exception) {
            Log.e("BeaconLoader", "Error parsing file $fileName: ${e.message}")
        }
    }

    return allBeacons
}

