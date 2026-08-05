package com.dbg.mdm_serverapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val running: Boolean = false,
    val lanAddress: String = "",
)

@Serializable
data class RegisterRequest(
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val appVersion: String,
)

@Serializable
data class RegisterResponse(
    val serverId: String,
    val accepted: Boolean,
    val message: String,
)

@Serializable
data class UpdateInfoRequest(
    val deviceId: String,
    val appVersion: String? = null,
    val facts: Map<String, String?> = emptyMap(),
)
