package com.dbg.mdm_serverapp.domain.event

import com.dbg.mdm_serverapp.domain.model.Device

/**
 * Domain events emitted when the device store changes.
 * Presentation reduces these into UI state; data layer publishes them.
 */
sealed class DeviceChangeEvent {
    data class Snapshot(
        val lanAddress: String,
        val devices: List<Device>,
        val onlineDeviceCount: Int,
    ) : DeviceChangeEvent()

    data class DeviceRegistered(
        val device: Device,
        val onlineDeviceCount: Int,
    ) : DeviceChangeEvent()

    data class DeviceUpdated(
        val deviceId: String,
        val lastSeenAt: Long,
        val onlineDeviceCount: Int,
    ) : DeviceChangeEvent()

    /** Local tick: recompute online from cached lastSeenAt without hitting the store. */
    data object PresenceTick : DeviceChangeEvent()
}
