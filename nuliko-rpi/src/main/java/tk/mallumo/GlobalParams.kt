package tk.mallumo

import java.io.File

/*
 java -jar ./Desktop/nuliko-rpi-1.0.0-all.jar \
--backup-dir "/tmp/backup" \
--backup-days 1 \
--cam-ip "192.168.1.16:8899" \
--cam-auth-name "cam1" \
--cam-auth-pass "Oscadnica993"

 */
object GlobalParams {

    var camIp = "192.168.1.16:8899"
        private set

    var camAuthName = "cam1"
        private set

    var camAuthPass = "Oscadnica993"
        private set

    var backupDays = 1
        private set

    var backupDir = File("/tmp/backup")
        private set

    fun init(args: Array<String>) {
        println("ARGS:")
        args.toList().chunked(2).forEach {
            println("${it.getOrNull(0)} <> ${it.getOrNull(1)}")
        }
        backupDir = args.getArgsParamFile("--backup-dir", backupDir)
        backupDays = args.getArgsParamInt("--backup-days", 1)
        camIp = args.getArgsParamString("--cam-ip", camIp)
        camAuthName = args.getArgsParamString("--cam-auth-name", camAuthName)
        camAuthPass = args.getArgsParamString("--cam-auth-pass", camAuthPass)
    }


}
