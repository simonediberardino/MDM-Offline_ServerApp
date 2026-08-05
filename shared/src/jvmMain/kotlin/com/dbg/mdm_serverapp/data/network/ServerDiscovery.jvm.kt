package com.dbg.mdm_serverapp.data.network

import com.dbg.mdm_serverapp.data.network.protocol.DiscoverReply
import com.dbg.mdm_serverapp.data.network.protocol.ProtocolConstants
import com.dbg.mdm_serverapp.data.network.protocol.parseDiscoverReply
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets

private suspend fun isStatusReachable(baseUrl: String): Boolean {
    val client = createHttpClient()
    try {
        val response = client.get("$baseUrl/status")
        return response.status.isSuccess()
    } catch (_: Exception) {
        return false
    } finally {
        client.close()
    }
}

private fun udpDiscover(): DiscoverReply? {
    var socket: DatagramSocket? = null
    try {
        socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = 1_500

        val requestBytes = ProtocolConstants.DISCOVER_REQUEST.toByteArray(StandardCharsets.UTF_8)
        val broadcast = InetAddress.getByName("255.255.255.255")
        val request = DatagramPacket(
            requestBytes,
            requestBytes.size,
            broadcast,
            ProtocolConstants.UDP_PORT,
        )
        socket.send(request)

        val buffer = ByteArray(512)
        val response = DatagramPacket(buffer, buffer.size)
        socket.receive(response)
        val raw = String(response.data, 0, response.length, StandardCharsets.UTF_8)
        return parseDiscoverReply(raw)
    } catch (_: SocketTimeoutException) {
        return null
    } catch (_: Exception) {
        return null
    } finally {
        if (socket != null) {
            try {
                socket.close()
            } catch (_: Exception) {
                // ignore
            }
        }
    }
}
