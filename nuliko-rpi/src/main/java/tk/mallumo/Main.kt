package tk.mallumo

import be.teletask.onvif.*
import tk.mallumo.io.*
import tk.mallumo.log.*
import java.lang.reflect.*
import kotlin.time.*


/*

https://github.com/03/ONVIF-Java

*/

/*
 java -jar ./nuliko-rpi-1.0.0-all.jar \
--backup-dir "/tmp/backup" \
--backup-days 1 \
--cam-ip "192.168.1.16:8899" \
--cam-auth-name "cam1" \
--cam-auth-pass "Oscadnica993"

 */
@ExperimentalTime
fun main(args: Array<String>) {

    GlobalParams.init(args)

    Repository.onvif.run()
    Repository.direct.run()

    stopUntilExit()

    Repository.close()
}

private fun stopUntilExit() {
    System.`in`.bufferedReader().use {
        while (true){
            if(it.readLine()?.trim() == "x") break
        }
    }
}
