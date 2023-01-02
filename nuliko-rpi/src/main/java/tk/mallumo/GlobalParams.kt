package tk.mallumo

import java.io.File

/*
 java -jar ./Desktop/nuliko-rpi-1.0.0-all.jar \
--backup-days 1 \
--cam-auth-pass "Oscadnica993"

 */
object GlobalParams {

    var camAuthPass = "Oscadnica993"
        private set

    var backupDays = 1
        private set

    var storageConnected = false
        private set

    private var backupDir: File? = null

    fun getCamDirectory(id: Int): File? = backupDir?.let {
        File(it, "cam_$id").apply {
            if (!exists()) mkdirs()
        }
    }

    fun init(args: Array<String>) {
        println("ARGS:")
        args.toList().chunked(2).forEach {
            println("${it.getOrNull(0)} <> ${it.getOrNull(1)}")
        }
        backupDays = args.getArgsParamInt("--backup-days", 1)
        camAuthPass = args.getArgsParamString("--cam-auth-pass", camAuthPass)

        connectExternalStorage()
    }

    private fun connectExternalStorage() {
        File("/media/pi")
            .listFiles()
            ?.firstOrNull { it.isDirectory && !it.isHidden }
            ?.also {
                backupDir = it
            }
    }


}
