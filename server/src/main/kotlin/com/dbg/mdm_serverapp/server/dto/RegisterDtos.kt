package com.dbg.mdm_serverapp.server.dto

import kotlinx.serialization.Serializable

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
