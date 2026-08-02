package com.dbg.mdm_serverapp.api

import com.dbg.mdm_serverapp.model.Device

data class ServerUiState(
    val running: Boolean = false,
    val lanAddress: String = "",
    val devices: List<Device> = emptyList(),
    val onlineDeviceCount: Int = 0,
)

fun ServerUiState.applyDbChange(event: DbChangeEvent): ServerUiState =
    when (event) {
        is DbChangeEvent.Snapshot -> copy(
            running = true,
            lanAddress = event.lanAddress,
            devices = event.devices.map { it.toDevice() },
            onlineDeviceCount = event.onlineDeviceCount,
        )

        is DbChangeEvent.DeviceRegistered -> {
            val device = event.device.toDevice()
            val without = devices.filterNot { it.id == device.id }
            copy(
                running = true,
                devices = listOf(device) + without,
                onlineDeviceCount = event.onlineDeviceCount,
            )
        }

        is DbChangeEvent.DeviceUpdated -> copy(
            running = true,
            onlineDeviceCount = event.onlineDeviceCount,
        )

        is DbChangeEvent.DevicesMarkedOffline -> copy(
            running = true,
            onlineDeviceCount = event.onlineDeviceCount,
        )
    }

fun StatusResponse.toUiState(): ServerUiState =
    ServerUiState(
        running = running,
        lanAddress = lanAddress,
        devices = devices.map { it.toDevice() },
        onlineDeviceCount = onlineDeviceCount,
    )

private fun StatusDeviceDto.toDevice(): Device =
    Device(
        id = id,
        name = name,
        platform = platform,
        registeredAt = registeredAt,
    )
