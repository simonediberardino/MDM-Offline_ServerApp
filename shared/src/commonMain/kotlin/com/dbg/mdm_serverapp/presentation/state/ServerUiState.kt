package com.dbg.mdm_serverapp.presentation.state

import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.Device
import com.dbg.mdm_serverapp.domain.model.DevicePresence
import com.dbg.mdm_serverapp.util.currentTimeMillis

data class ServerUiState(
    val running: Boolean = false,
    val lanAddress: String = "",
    val devices: List<Device> = emptyList(),
    val onlineDeviceCount: Int = 0,
)

fun ServerUiState.applyDeviceChange(
    event: DeviceChangeEvent,
    nowMs: Long = currentTimeMillis(),
): ServerUiState {
    val next = when (event) {
        is DeviceChangeEvent.Snapshot -> copy(
            running = true,
            lanAddress = event.lanAddress,
            devices = event.devices,
        )

        is DeviceChangeEvent.DeviceRegistered -> {
            val without = devices.filterNot { it.id == event.device.id }
            copy(
                running = true,
                devices = listOf(event.device) + without,
            )
        }

        is DeviceChangeEvent.DeviceUpdated -> copy(
            running = true,
            devices = devices.map { device ->
                if (device.id != event.deviceId) device
                else device.copy(lastSeenAt = event.lastSeenAt)
            },
        )

        is DeviceChangeEvent.PresenceTick -> this
    }

    val devices = next.devices.map { device ->
        device.copy(online = DevicePresence.isOnline(device.lastSeenAt, nowMs))
    }
    return next.copy(
        devices = devices,
        onlineDeviceCount = devices.count { it.online },
    )
}
