package com.dbg.mdm_serverapp.server.data.network

import com.dbg.mdm_serverapp.server.LanAddressResolver
import com.dbg.mdm_serverapp.server.ServerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Listens for LAN broadcast discovery requests and replies with this
 * machine's local IPv4 and HTTP port.
 */
class UdpDiscoveryResponder(
    private val udpPort: Int,
    private val httpPort: Int,
) {
    private val logger: Logger = LoggerFactory.getLogger(UdpDiscoveryResponder::class.java)
    private val running = AtomicBoolean(false)
    private var socket: DatagramSocket? = null
    private var thread: Thread? = null

    constructor() : this(ServerConfig.UDP_PORT, ServerConfig.HTTP_PORT)

    fun start() {
        if (!running.compareAndSet(false, true)) {
            return
        }

        val worker = Thread(
            { listenLoop() },
            "mdm-udp-discovery",
        )
        worker.isDaemon = true
        thread = worker
        worker.start()

        logger.info("UDP discovery listening on port $udpPort")
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) {
            return
        }

        val currentSocket = socket
        if (currentSocket != null) {
            try {
                currentSocket.close()
            } catch (ignored: Exception) {
                // Ignore close errors during shutdown.
            }
        }
        socket = null
        thread = null
    }

    private fun listenLoop() {
        var datagramSocket: DatagramSocket? = null
        try {
            datagramSocket = DatagramSocket(null)
            datagramSocket.reuseAddress = true
            datagramSocket.bind(InetSocketAddress(udpPort))
            datagramSocket.broadcast = true
            socket = datagramSocket

            val buffer = ByteArray(512)
            while (running.get()) {
                val request = DatagramPacket(buffer, buffer.size)
                try {
                    datagramSocket.receive(request)
                } catch (ex: SocketException) {
                    if (!running.get()) {
                        break
                    }
                    continue
                }

                val message = String(request.data, 0, request.length, StandardCharsets.UTF_8).trim()
                if (message != ServerConfig.DISCOVER_REQUEST) {
                    continue
                }

                val localIp = LanAddressResolver.primaryIpv4()
                val reply = ServerConfig.DISCOVER_RESPONSE_PREFIX + "|" + localIp + "|" + httpPort
                val replyBytes = reply.toByteArray(StandardCharsets.UTF_8)
                val response = DatagramPacket(
                    replyBytes,
                    replyBytes.size,
                    request.address,
                    request.port,
                )

                try {
                    datagramSocket.send(response)
                    logger.info(
                        "Discovery reply " + reply + " -> " +
                            request.address.hostAddress + ":" + request.port,
                    )
                } catch (error: Exception) {
                    logger.warn("Failed to send discovery reply: " + error.message)
                }
            }
        } catch (error: Exception) {
            if (running.get()) {
                logger.error("UDP discovery stopped unexpectedly: " + error.message, error)
            }
        } finally {
            running.set(false)
            if (datagramSocket != null) {
                try {
                    datagramSocket.close()
                } catch (ignored: Exception) {
                    // Ignore close errors.
                }
            }
            socket = null
        }
    }
}
