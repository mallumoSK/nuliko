@file:Suppress("unused")

package tk.mallumo.nuliko.android.system

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import tk.mallumo.utils.Shell
import tk.mallumo.utils.shellSH
import tk.mallumo.utils.shellSU
import java.io.File
import java.io.StringReader
import kotlin.reflect.KClass

@JvmInline
private value class SystemPermission(private val data: Pair<String, Int>) {
    val permission get() = data.first
    val androidMinVersion get() = data.second

    companion object {
        const val SYSTEM = -1
    }
}

private val notGrantablePermissions = arrayOf(
    SystemPermission("android.permission.BLUETOOTH_CONNECT" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.BLUETOOTH_SCAN" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.REBOOT" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.DEVICE_POWER" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.CLEAR_APP_CACHE" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.INTERACT_ACROSS_USERS_FULL" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.REQUEST_INSTALL_PACKAGES" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.INSTALL_PACKAGES" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.FOREGROUND_SERVICE" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.MANAGE_EXTERNAL_STORAGE" to SystemPermission.SYSTEM),
    SystemPermission("com.google.android.c2dm.permission.RECEIVE" to SystemPermission.SYSTEM),
    SystemPermission("com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE" to SystemPermission.SYSTEM),
    SystemPermission("com.google.android.gms.permission.AD_ID" to SystemPermission.SYSTEM),
    SystemPermission("android.permission.SCHEDULE_EXACT_ALARM" to 31),
)

fun Context.getNotGrantedPermissions(): List<String> {
    val permissionDiff = notGrantablePermissions
        .asSequence()
        .filter { it.androidMinVersion == SystemPermission.SYSTEM || it.androidMinVersion >= Build.VERSION.SDK_INT }
        .map { it.permission }

    return packageManager
        .runCatching {
            getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                .filterNot { it in permissionDiff }
                .filter {
                    ContextCompat.checkSelfPermission(
                        this@getNotGrantedPermissions,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                }
        }.getOrNull()
        ?: listOf()
}

fun Shell.disableBatterySaver() {
    val data =
        "quick_doze_enabled=false,force_all_apps_standby=false,force_background_check=false,optional_sensors_disabled=false,launch_boost_disabled=false,firewall_disabled=true"

    run("dumpsys deviceidle disable all")

    runConditionCommand(
        query = "settings get global battery_saver_constants",
        expects = data,
        command = "settings put global battery_saver_constants \"$data\""
    )
    runConditionCommand(
        query = "settings get global hdmi_control_enabled",
        expects = "0",
        command = "settings put global hdmi_control_enabled 0"
    )
    runConditionCommand(
        query = "settings get global stay_on_while_plugged_in",
        expects = "1",
        command = "settings put global stay_on_while_plugged_in 1"
    )
    runConditionCommand(
        query = "settings get secure screensaver_enabled",
        expects = "0",
        command = "settings put secure screensaver_enabled 0"
    )


    arrayOf(
        "com.tapgest.tapservice",
        "com.tapgest.gestai",
        "com.tapgest.admin",
        "com.tapgest.remote.controller.android",
    ).onEach { pckg ->
        runConditionCommand(
            query = "dumpsys deviceidle except-idle-whitelist =$pckg",
            expects = "true",
            command = "dumpsys deviceidle whitelist +$pckg"
        )
    }
}

fun Shell.setupDeviceDefaults() {
    //WIFI
    runConditionCommand(
        query = "settings get global airplane_mode_on",
        expects = "0",
        command = "settings put global airplane_mode_on 0"
    )
    runConditionCommand(
        query = "settings get global wifi_on",
        expects = "1",
        command = "settings put global wifi_on 1"
    )
    runConditionCommand(
        query = "settings get global wifi_scan_always_enabled",
        expects = "1",
        command = "settings put global wifi_scan_always_enabled 1"
    )

    //DEBUGG
    runConditionCommand(
        query = "settings get global development_settings_enabled",
        expects = "1",
        command = "settings put global development_settings_enabled 1"
    )
    runConditionCommand(
        query = "getprop service.adb.tcp.port",
        expects = "5555",
        command = "setprop service.adb.tcp.port 5555"
    )

    //APK
    runConditionCommand(
        query = "settings get global package_verifier_enable",
        expects = "-1",
        command = "settings put global package_verifier_enable -1"
    )
    runConditionCommand(
        query = "settings get global package_verifier_user_consent",
        expects = "-1",
        command = "settings put global package_verifier_user_consent -1"
    )
    runConditionCommand(
        query = "settings get global device_provisioned",
        expects = "0",
        command = "settings put global device_provisioned 0"
    )
    setupKeyboard()
}

fun Shell.setupHomeLauncherDefaults(
    context: Context,
    kClass: KClass<out Activity>,
    disableSystemUI: Boolean,
    rebootEnabled: Boolean,
) {
    //LAUNCHER
    run("cmd package set-home-activity \"${context.packageName}/${kClass.qualifiedName}\"").printInfo()
    if (disableSystemUI) {
        runCondition(
            query = "pm list packages -d 2>/dev/null | grep com.android.systemui | wc -l",
            condition = { it.output() == "1" },
            reject = {
                run("pm disable com.android.systemui").printInfo()
                if (rebootEnabled) run("reboot").printInfo()
            })
    }
}

fun Shell.grantManagedStoragePermissions(pckg: String) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        run("appops set --uid $pckg MANAGE_EXTERNAL_STORAGE allow")
            .printInfo()
    }
}

fun Shell.grantRuntimePermissions(context: Context): List<String> {
    context.getNotGrantedPermissions().forEach {
        it.runCatching {
            run("pm grant ${context.packageName} $this").printInfo()
        }
    }
    return context.getNotGrantedPermissions()
}

private val requiredLibs = setOf(
    "libsystemcontrol_jni.so",
    "libtv_jni.so",
    "libscreencontrol_jni.so",
    "libCLC.so",
    "libGAL.so",
    "libOpenVX.so",
    "libOpenVXU.so",
    "libVSC.so",
    "libarchmodelSw.so",
    "libNNArchPerf.so"
)


private fun Shell.setupKeyboard() {
    runConditionCommand(
        query = "settings get secure show_ime_with_hard_keyboard",
        expects = "1",
        command = "settings put secure show_ime_with_hard_keyboard 1"
    )
    run("ime set com.tapgest.admin/.service.SimpleKeyboardService")
}

fun Shell.setupKioskMode(enable: Boolean) {
    if (enable) enableKiosk()
    else disableKiosk()
}

private fun Shell.disableKiosk() {
    runConditionCommand(
        query = "settings get secure sysui_nav_bar",
        expects = "null",
        command = "settings put secure sysui_nav_bar null"
    )
}

private fun Shell.enableKiosk() {
    runConditionCommand(
        query = "settings get secure sysui_nav_bar",
        expects = "space;space;space",
        command = "settings put secure sysui_nav_bar \"space;space;space\""
    )
}

/**
### ACTIVATE:
adb shell dpm set-active-admin  com.tapgest.admin/.broadcast.AdminReceiver

adb shell dpm set-device-owner com.tapgest.admin/.broadcast.AdminReceiver

### Deactivate:
adb shell dpm remove-active-admin com.tapgest.admin/.service.AdminReceiver
 *
 */
fun <T : BroadcastReceiver> Shell.activateAdminMode(activity: Context, receiverBroadcast: KClass<T>, isDebugMode: Boolean) {
    if (!isDebugMode) {
        run("dpm set-active-admin ${activity.packageName}/${receiverBroadcast.qualifiedName}").printInfo()
        run("dpm set-device-owner ${activity.packageName}/${receiverBroadcast.qualifiedName}").printInfo()
    }
    runConditionCommand(
        query = "settings get global package_verifier_enable",
        expects = "-1",
        command = "settings put global package_verifier_enable -1"
    )
    runConditionCommand(
        query = "settings get global package_verifier_user_consent",
        expects = "-1",
        command = "settings put global package_verifier_user_consent -1"
    )
    runConditionCommand(
        query = "settings get global device_provisioned",
        expects = "0",
        command = "settings put global device_provisioned 0"
    )
    runConditionCommand(
        query = "settings get global device_provisioned",
        expects = "0",
        command = "settings put global device_provisioned 0"
    )
    runConditionCommand(
        query = "settings get secure sysui_nav_bar",
        expects = "space;space;space",
        command = "settings put secure sysui_nav_bar \"space;space;space\""
    )
}

//com.android.launcher3/com.android.launcher3.Launcher
private fun Shell.runCondition(
    query: String,
    condition: (Shell.Command.Result) -> Boolean,
    grant: () -> Unit = {},
    reject: () -> Unit = {}
) {
    run(query)
        .printInfo()
        .takeIf(condition)
        .also {
            if (it == null) reject()
            else grant()
        }
}

internal fun Shell.runConditionCommand(query: String, expects: String, command: String, afterChange: () -> Unit = {}) {
    run(query)
        .printInfo()
        .takeIf { it.output().trim() != expects }
        ?.also {
            run(command).printInfo()
            afterChange()
        }
}

private fun Shell.runConditionCommand(query: String, expects: (String) -> Boolean, command: String) {
    run(query)
        .printInfo()
        .takeIf { !expects(it.output().trim()) }
        ?.also {
            run(command).printInfo()
        }
}

private fun Shell.Command.Result.printInfo(): Shell.Command.Result {
    this.details.command
    if (isSuccess) Log.i("shell", "${details.command} : ${output()}")
    else Log.e("shell", "${details.command} : ${stderr()}")
    return this
}

data class CmdResponse(
    val ok: Boolean,
    val outputs: List<String>,
    val errors: List<String>,
    val output: String = outputs.joinToString("\n"),
    val error: String = errors.joinToString("\n"),
)

fun cmd(command: String): CmdResponse =cmd {
    command
}

fun cmd(body: () -> String): CmdResponse = try {
    shellSU {
        run(body()).let {
           CmdResponse(
                ok = it.isSuccess,
                outputs = it.output,
                errors = it.stderr
            )
        }
    }
} catch (e: Shell.NotFoundException) {
    try {
        shellSH {
            run(body()).let {
                 CmdResponse(
                    ok = it.isSuccess,
                    outputs = it.output,
                    errors = it.stderr
                )
            }
        }
    } catch (e1: Throwable) {
        CmdResponse(
            ok = false,
            outputs = listOf(),
            errors = StringReader(Log.getStackTraceString(e)).use {
                it.readLines()
            }
        )
    }
} catch (e: Throwable) {
    CmdResponse(
        ok = false,
        outputs = listOf(),
        errors = StringReader(Log.getStackTraceString(e)).use {
            it.readLines()
        }
    )
}

//fun Shell.grantNpuPermissions() {
//    if (!Constants.isKhadasVim3()) return
//    run("cat /vendor/etc/public.libraries.txt")
//        .printInfo()
//        .output
//        .takeUnless { it.containsAll(requiredLibs) }
//        ?.also {
//            Log.w("NPU_LIBS", "npu init start")
//            run("mount -o rw,remount /vendor").also { result ->
//                if (result.isSuccess) Log.w("NPU_LIBS", result.stdout())
//                else Log.e("NPU_LIBS", result.stderr())
//            }
//            run(
//                "printf \"${
//                    (requiredLibs + it).joinToString(
//                        separator = "\n",
//                        postfix = "\n\n"
//                    )
//                }\" > /vendor/etc/public.libraries.txt"
//            ).also { writeResult ->
//                if (writeResult.isSuccess) Log.i("NPU_LIBS", "permission GRANTED")
//                else Log.e("NPU_LIBS", writeResult.stderr())
//            }
//            Log.w("NPU_LIBS", "npu init fin")
//            run("reboot")
//        }
//}