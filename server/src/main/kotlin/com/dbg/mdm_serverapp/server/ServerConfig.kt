package com.dbg.mdm_serverapp.server

object ServerConfig {
    const val HTTP_PORT = 9876
    const val UDP_PORT = 9877
    /** Client-side HTTP server (`GET /ping`). */
    const val CLIENT_HTTP_PORT = 9878

    /** Client-side UDP socket (discover send/receive + future inbound messages). */
    const val CLIENT_UDP_PORT = 9879

    /** Payload clients broadcast to ask for the server address. */
    const val DISCOVER_REQUEST = "MDM_DISCOVER"

    /** Reply prefix: `MDM_SERVER|<localIp>|<httpPort>` */
    const val DISCOVER_RESPONSE_PREFIX = "MDM_SERVER"
}
