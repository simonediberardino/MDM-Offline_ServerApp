package com.dbg.mdm_serverapp.protocol

object ProtocolConstants {
    const val HTTP_PORT = 9876
    const val UDP_PORT = 9877

    /** Payload clients broadcast to ask for the server address. */
    const val DISCOVER_REQUEST = "MDM_DISCOVER"

    /** Reply prefix: `MDM_SERVER|<localIp>|<httpPort>` */
    const val DISCOVER_RESPONSE_PREFIX = "MDM_SERVER"
}

fun buildDiscoverReply(localIp: String, httpPort: Int = ProtocolConstants.HTTP_PORT): String =
    "${ProtocolConstants.DISCOVER_RESPONSE_PREFIX}|$localIp|$httpPort"

data class DiscoverReply(
    val localIp: String,
    val httpPort: Int,
)

fun parseDiscoverReply(raw: String): DiscoverReply? {
    val parts = raw.trim().split('|')
    if (parts.size < 3) return null
    if (parts[0] != ProtocolConstants.DISCOVER_RESPONSE_PREFIX) return null
    val port = parts[2].toIntOrNull() ?: return null
    return DiscoverReply(localIp = parts[1], httpPort = port)
}
