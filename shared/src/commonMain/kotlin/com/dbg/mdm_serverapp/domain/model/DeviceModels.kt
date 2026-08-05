package com.dbg.mdm_serverapp.domain.model

/** Identity attributes captured at registration (list / overview). */
data class Device(
    val id: String,
    val name: String,
    val platform: String,
    val registeredAt: Long,
)

/** Mutable runtime attributes (detail page). */
data class DeviceInfo(
    val deviceId: String,
    val appVersion: String,
    val lastSeenAt: Long,
    val online: Boolean,
    val remoteAddress: String? = null,
)

/** Sparse per-device key/value fact (optional attributes). */
data class DeviceFact(
    val deviceId: String,
    val key: String,
    val value: String,
    val updatedAt: Long,
)

/** Full device view for the detail page. */
data class DeviceDetail(
    val device: Device,
    val info: DeviceInfo,
    val facts: List<DeviceFact> = emptyList(),
)

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    ITALIAN("it");

    companion object {
        /** Uses the language part of a locale tag (e.g. "it-IT" → Italian). Falls back to English. */
        fun fromLocaleTag(tag: String): AppLanguage {
            val language = tag
                .substringBefore('-')
                .substringBefore('_')
                .lowercase()
            return entries.firstOrNull { it.code == language } ?: ENGLISH
        }
    }
}
