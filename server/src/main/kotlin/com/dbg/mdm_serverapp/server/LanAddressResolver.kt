package com.dbg.mdm_serverapp.server

import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

object LanAddressResolver {
    fun primaryIpv4(): String {
        ipv4WithInternet()?.let { return it }

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

    /**
     * Local IPv4 the OS would use for default internet routing.
     * Uses a UDP "connect" (no packets sent) against a public address.
     */
    private fun ipv4WithInternet(): String? =
        try {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 53)
                val local = socket.localAddress
                if (local is Inet4Address && !local.isLoopbackAddress) {
                    local.hostAddress?.takeUnless { it.startsWith("169.254.") }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
}
