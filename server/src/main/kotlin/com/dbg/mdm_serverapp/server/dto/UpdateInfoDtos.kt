package com.dbg.mdm_serverapp.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfoRequest(
    val deviceId: String,
    val appVersion: String? = null,
    val facts: Map<String, String?> = emptyMap(),
)

