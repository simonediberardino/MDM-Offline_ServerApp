package com.dbg.mdm_serverapp.data.network

import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-process bus of device-store change events.
 * Server data layer publishes here; [com.dbg.mdm_serverapp.domain.repository.DeviceRepository]
 * exposes [events] to presentation.
 */
class DeviceChangeBus {
    private val _events = MutableSharedFlow<DeviceChangeEvent>(
        extraBufferCapacity = 64,
    )
    val events: SharedFlow<DeviceChangeEvent> = _events.asSharedFlow()

    fun publish(event: DeviceChangeEvent) {
        _events.tryEmit(event)
    }
}
