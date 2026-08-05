package com.dbg.mdm_serverapp.presentation.state

import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.Device

data class ServerUiState(
    val running: Boolean = false,
    val lanAddress: String = "",
    val devices: List<Device> = emptyList(),
    val onlineDeviceCount: Int = 0,
)

fun ServerUiState.applyDeviceChange(event: DeviceChangeEvent): ServerUiState =
    when (event) {
        is DeviceChangeEvent.Snapshot -> copy(
            running = true,
            lanAddress = event.lanAddress,
            devices = event.devices,
            onlineDeviceCount = event.onlineDeviceCount,
        )

        is DeviceChangeEvent.DeviceRegistered -> {
            val without = devices.filterNot { it.id == event.device.id }
            copy(
                running = true,
                devices = listOf(event.device) + without,
                onlineDeviceCount = event.onlineDeviceCount,
            )
        }

        is DeviceChangeEvent.DeviceUpdated -> copy(
            running = true,
            onlineDeviceCount = event.onlineDeviceCount,
        )

        is DeviceChangeEvent.DevicesMarkedOffline -> copy(
            running = true,
            onlineDeviceCount = event.onlineDeviceCount,
        )
    }
