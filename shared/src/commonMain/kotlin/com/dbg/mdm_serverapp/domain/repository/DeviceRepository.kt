package com.dbg.mdm_serverapp.domain.repository

import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the device store for the presentation layer.
 * Desktop wires an in-process implementation backed by the local DB + change bus.
 */
interface DeviceRepository {
    /** Live stream of store mutations after the initial [getSnapshot]. */
    fun observeChanges(): Flow<DeviceChangeEvent>

    /** Current full list + LAN address for cold start / refresh. */
    fun getSnapshot(): DeviceChangeEvent.Snapshot

    /** Detail page payload, or null if the device is unknown. */
    fun getDeviceDetail(deviceId: String): DeviceDetail?
}
