package com.dbg.mdm_serverapp.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusDeviceDto(
    val id: String,
    val name: String,
    val platform: String,
    val registeredAt: Long,
)

@Serializable
sealed class DbChangeEvent {
    @Serializable
    @SerialName("SNAPSHOT")
    data class Snapshot(
        val lanAddress: String,
        val devices: List<StatusDeviceDto>,
        val onlineDeviceCount: Int,
    ) : DbChangeEvent()

    @Serializable
    @SerialName("DEVICE_REGISTERED")
    data class DeviceRegistered(
        val device: StatusDeviceDto,
        val onlineDeviceCount: Int,
    ) : DbChangeEvent()

    @Serializable
    @SerialName("DEVICE_UPDATED")
    data class DeviceUpdated(
        val deviceId: String,
        val onlineDeviceCount: Int,
    ) : DbChangeEvent()

    @Serializable
    @SerialName("DEVICES_MARKED_OFFLINE")
    data class DevicesMarkedOffline(
        val onlineDeviceCount: Int = 0,
    ) : DbChangeEvent()
}

@Serializable
data class StatusResponse(
    val running: Boolean = false,
    val lanAddress: String = "",
    val devices: List<StatusDeviceDto> = emptyList(),
    val onlineDeviceCount: Int = 0,
)

fun StatusResponse.toSnapshotEvent(): DbChangeEvent.Snapshot =
    DbChangeEvent.Snapshot(
        lanAddress = lanAddress,
        devices = devices,
        onlineDeviceCount = onlineDeviceCount,
    )
