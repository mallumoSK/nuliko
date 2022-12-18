package tk.mallumo

import tk.mallumo.io.*
import kotlin.time.*


/*

https://github.com/03/ONVIF-Java

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
