package tk.mallumo.nuliko.android.system

import android.content.Context
import kotlinx.coroutines.runBlocking
import java.io.File

@Suppress("unused")
object AndroidCMD {

    val firmware: String?
        get() = runBlocking {
            cmd("getprop ro.build.version.incremental")
                .takeIf { it.ok }
                ?.output
        }

    val isRooted by lazy {
        try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: Throwable) {
            false
        }
    }

    val manufacturer: String?
        get() = runBlocking {
            cmd("getprop ro.product.manufacturer")
                .takeIf { it.ok }
                ?.output
        }

    val model: String?
        get() = runBlocking {
            cmd("getprop ro.product.model")
                .takeIf { it.ok }
                ?.output
        }


    val macWifi: String?
        get() = runBlocking {
            mac("wlan0")
        }

    val macEth: String?
        get() = runBlocking {
            mac("eth0")
        }

    val ipWifi: String?
        get() = runBlocking {
            ip("wlan0")
        }

    val ipEth: String?
        get() = runBlocking {
            ip("eth0")
        }

    fun mac(device: String) = cmd("ip addr show $device")
        .takeIf { it.ok }
        ?.outputs
        ?.takeIf { strings -> strings.any { it.contains(device) } }
        ?.drop(1)
        ?.firstOrNull()
        ?.trim()
        ?.split(" ")
        ?.getOrNull(1)

    fun ip(device: String) = cmd("ip addr show $device")
        .takeIf { it.ok }
        ?.outputs
        ?.firstOrNull { it.contains("inet") && it.contains(device) }
        ?.trim()
        ?.split(" ")
        ?.getOrNull(1)

    fun screenshot(png: File) = cmd("screencap -p ${png.absolutePath}")

    fun reboot(context: Context, force: Boolean = false) {
        cmd("reboot")
    }

    fun screenOFF() = cmd("input keyevent KEYCODE_SLEEP")

    fun screenON() = cmd("input keyevent KEYCODE_WAKEUP")

    // cat /sys/class/thermal/thermal_zone0/temp
    fun getThermalValue(index: Int): Float? =
        cmd("cat /sys/class/thermal/thermal_zone$index/temp")
            .output
            .trim()
            .toFloatOrNull()
            ?.let {
                it / 1_000F
            }
}