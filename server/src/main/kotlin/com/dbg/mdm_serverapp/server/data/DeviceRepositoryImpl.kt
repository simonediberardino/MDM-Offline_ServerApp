package com.dbg.mdm_serverapp.server.data

import com.dbg.mdm_serverapp.data.network.DeviceChangeBus
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.server.data.local.db.MdmDatabase
import kotlinx.coroutines.flow.Flow

/**
 * In-process [DeviceRepository] backed by SQLite + [DeviceChangeBus].
 */
class DeviceRepositoryImpl(
    private val database: MdmDatabase,
    private val changeBus: DeviceChangeBus,
    private val lanAddressProvider: () -> String,
) : DeviceRepository {
    override fun observeChanges(): Flow<DeviceChangeEvent> = changeBus.events

    override fun getSnapshot(): DeviceChangeEvent.Snapshot =
        database.snapshot(lanAddressProvider())

    override fun getDeviceDetail(deviceId: String): DeviceDetail? =
        database.getDeviceDetail(deviceId)
}
