package com.dbg.mdm_serverapp.domain.model

/** Presence rules derived from [DeviceInfo.lastSeenAt] / [Device.lastSeenAt]. */
object DevicePresence {
    const val ONLINE_THRESHOLD_MS = 5 * 60 * 1000L
    const val ONLINE_RECOMPUTE_INTERVAL_MS = 30_000L

    fun isOnline(lastSeenAt: Long, nowMs: Long): Boolean =
        nowMs - lastSeenAt < ONLINE_THRESHOLD_MS
}
