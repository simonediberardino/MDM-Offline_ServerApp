package com.dbg.mdm_serverapp.server.model

/** Identity attributes captured at registration. */
data class Device(
    val id: String,
    val name: String,
    val platform: String,
    val registeredAt: Long,
)

/** Mutable runtime attributes. */
data class DeviceInfo(
    val deviceId: String,
    val appVersion: String,
    val lastSeenAt: Long,
    val online: Boolean,
    val remoteAddress: String? = null,
)
