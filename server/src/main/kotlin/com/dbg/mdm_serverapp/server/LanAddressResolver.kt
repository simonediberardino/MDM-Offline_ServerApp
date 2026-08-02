package com.dbg.mdm_serverapp.server

import java.net.Inet4Address
import java.net.NetworkInterface

object LanAddressResolver {
    fun primaryIpv4(): String {
        val candidates = mutableListOf<String>()
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
        for (networkInterface in interfaces) {
            if (!networkInterface.isUp || networkInterface.isLoopback) continue
            for (address in networkInterface.inetAddresses) {
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    candidates += address.hostAddress
                }
            }
        }
        return candidates.firstOrNull { !it.startsWith("169.254.") }
            ?: candidates.firstOrNull()
            ?: "127.0.0.1"
    }
}
