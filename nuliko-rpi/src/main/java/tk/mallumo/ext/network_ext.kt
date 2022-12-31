package tk.mallumo.ext

import java.net.Inet4Address
import java.net.NetworkInterface

val NetworkInterface.ip: String?
    get() = inetAddresses.asSequence()
        .filterIsInstance<Inet4Address>()
        .firstOrNull()
        ?.hostAddress
        ?.takeIf { !it.startsWith("127") }