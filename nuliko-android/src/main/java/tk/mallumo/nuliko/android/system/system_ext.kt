package tk.mallumo.nuliko.android.system

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import tk.mallumo.utils.tryPrint
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

private const val defaultMAC = "02:00:00:00:00:00"

@Suppress("unused")
val Application.deviceID
    @SuppressLint("HardwareIds")
    get() = getWifiMac()
        .takeIf { it != defaultMAC }
        ?.takeIf { it.isNotEmpty() }
        ?.replace(":", "")
        ?.uppercase()
        ?: generateID()

@SuppressLint("SdCardPath")
private fun generateID(): String {
    val file =  File("/sdcard/device-id")
  return  file.let {
        var currentID =
            if (it.exists()) it.readText()
            else null

        if (currentID.isNullOrEmpty()) {
            currentID = UUID.randomUUID().toString()
                .replace("-", "")
                .replace(":", "")
                .uppercase()
//            file.writeText(currentID)
        }
        currentID
    }
}

@Suppress("unused", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
val Application.ipAddress: List<String>
    get() = networkInfoHW.map { it.ipv4 }

private fun Context.getWifiMac(): String =
    networkInfoHW
        .firstOrNull { it.adapter == "wlan0" }
        ?.mac
        ?: defaultMAC

fun Context.openAndroidSettings() {
    try {
        packageManager.getLaunchIntentForPackage("com.droidlogic.tv.settings")
            .also { startActivity(it) }
    } catch (e: Exception) {
        try {
            startActivity(Intent().apply {
                setClassName("com.android.tv.settings","com.android.tv.settings.MainSettings")
            })
        } catch (e: Exception) {
            e.printStackTrace()
            tryPrint {
                PendingIntent.getActivity(
                    this,
                    5682,
                    Intent(Settings.ACTION_SETTINGS),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ).send()
            }
        }
    }
}

@Suppress("unused")
val Context.networkInfoHW: List<NetworkInfoHW>
    get() = cmd("ifconfig")
        .takeIf { it.ok }
        ?.takeIf { it.outputs.isNotEmpty() }
        ?.outputs
        ?.toMutableList()
        ?.runCatching {
            val devices = mutableListOf<NetworkInfoHW>()
            while (isNotEmpty()) {
                val splitIndex = indexOfFirst { it.trim().isEmpty() }
                if (splitIndex == -1) {
                    buildDevice(subList(0, size))?.also {
                        devices.add(it)
                    }
                    clear()
                } else {
                    buildDevice(subList(0, splitIndex))?.also {
                        devices.add(it)
                    }
                    while (!firstOrNull().isNullOrBlank()) {
                        removeFirst()
                    }
                    removeFirst()
                }
            }
            devices
        }?.getOrNull()
        ?: networkInfoSecondaryHW

private fun buildDevice(subList: MutableList<String>): NetworkInfoHW? {
    if (subList.isEmpty()) return null

    val adapter = subList.firstOrNull()
        ?.let { it.substring(0, it.indexOfFirst { it.isWhitespace() }) }
        ?.takeIf { it !in arrayOf("lo", "dummy0") }
        ?: return null

    val mac = subList.firstOrNull { it.contains("HWaddr") }
        ?.split(" ")
        ?.let {
            it[it.indexOf("HWaddr") + 1].uppercase()
        } ?: return null

    val ip = subList.firstOrNull { it.contains("inet addr:") }
        ?.split(" ")
        ?.let {
            it[it.indexOf("inet") + 1].split(":")[1]
        }

    return NetworkInfoHW(
        adapter = adapter,
        mac = mac,
        ipv4 = ip ?: ""
    )
}

@Suppress("unused")
private val Context.networkInfoSecondaryHW: List<NetworkInfoHW>
    get() = NetworkInterface.getNetworkInterfaces()
        .toList()
        .map { adapter ->
            NetworkInfoHW(
                adapter = adapter.name,
                mac = adapter.hardwareAddress?.joinToString(":") { String.format("%02X", it).uppercase() } ?: "",
                ipv4 = adapter.inetAddresses.toList().firstOrNull { it is Inet4Address }?.hostAddress ?: ""
            )
        }
        .filter { it.mac.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?: networkInfoTercialHW

@Suppress("unused")
private val Context.networkInfoTercialHW: List<NetworkInfoHW>
    get() = runBlocking {
        listOf("eth0", "wlan0")
            .map {
                NetworkInfoHW(
                    adapter = it,
                    mac = AndroidCMD.mac(it)?.uppercase() ?: "",
                    ipv4 = AndroidCMD.ip(it)?.uppercase() ?: ""
                )
            }
            .filter { it.mac.isNotEmpty() }
    }

@Serializable
data class NetworkInfoHW(
    var adapter: String = "",
    var mac: String = "",
    var ipv4: String = ""
)