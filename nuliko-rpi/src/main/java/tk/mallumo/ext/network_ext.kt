package tk.mallumo.ext

import java.net.Inet4Address
import java.net.NetworkInterface

val NetworkInterface.ip: String?
    get() = inetAddresses.asSequence()
        .filterNot { it.isLoopbackAddress }
//        .filterNot { it.na }
        .filterIsInstance<Inet4Address>()
        .firstOrNull()
        ?.hostAddress