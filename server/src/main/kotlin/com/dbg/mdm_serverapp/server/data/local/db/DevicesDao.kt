package com.dbg.mdm_serverapp.server.data.local.db

import com.dbg.mdm_serverapp.data.network.DeviceChangeBus
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.Device
import com.dbg.mdm_serverapp.domain.model.DeviceInfo
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class DevicesDao(
    private val database: Database,
    private val changeBus: DeviceChangeBus? = null,
) {
    /**
     * Inserts immutable device fields only on first registration;
     * always refreshes mutable info columns.
     */
    fun register(
        id: String,
        name: String,
        platform: String,
        appVersion: String,
        online: Boolean,
        remoteAddress: String?,
        now: Long = System.currentTimeMillis(),
    ): Device = transaction(database) {
        val existing = find(id)
        if (existing == null) {
            DevicesTable.insert {
                it[DevicesTable.id] = id
                it[DevicesTable.name] = name
                it[DevicesTable.platform] = platform
                it[DevicesTable.registeredAt] = now
                it[DevicesTable.appVersion] = appVersion
                it[DevicesTable.lastSeenAt] = now
                it[DevicesTable.online] = online
                it[DevicesTable.remoteAddress] = remoteAddress
            }
            val device = find(id)!!
            publish(
                DeviceChangeEvent.DeviceRegistered(
                    device = device,
                    onlineDeviceCount = countOnline(),
                ),
            )
            device
        } else {
            applyInfoUpdate(
                deviceId = id,
                appVersion = appVersion,
                online = online,
                remoteAddress = remoteAddress,
                now = now,
            )
            find(id)!!
        }
    }

    fun updateInfo(
        deviceId: String,
        appVersion: String? = null,
        online: Boolean? = null,
        remoteAddress: String? = null,
        now: Long = System.currentTimeMillis(),
        publishChange: Boolean = true,
    ) {
        transaction(database) {
            applyInfoUpdate(
                deviceId = deviceId,
                appVersion = appVersion,
                online = online,
                remoteAddress = remoteAddress,
                now = now,
                publishChange = publishChange,
            )
        }
    }

    fun setOnline(id: String, online: Boolean, remoteAddress: String? = null) {
        updateInfo(
            deviceId = id,
            online = online,
            remoteAddress = remoteAddress,
        )
    }

    fun markAllOffline() {
        transaction(database) {
            DevicesTable.update({ DevicesTable.online eq true }) {
                it[online] = false
            }
            publish(DeviceChangeEvent.DevicesMarkedOffline(onlineDeviceCount = 0))
        }
    }

    fun get(id: String): Device? = transaction(database) {
        find(id)
    }

    fun getInfo(deviceId: String): DeviceInfo? = transaction(database) {
        DevicesTable.selectAll()
            .where { DevicesTable.id eq deviceId }
            .map { row ->
                DeviceInfo(
                    deviceId = row[DevicesTable.id],
                    appVersion = row[DevicesTable.appVersion],
                    lastSeenAt = row[DevicesTable.lastSeenAt],
                    online = row[DevicesTable.online],
                    remoteAddress = row[DevicesTable.remoteAddress],
                )
            }
            .singleOrNull()
    }

    fun list(): List<Device> = transaction(database) {
        DevicesTable.selectAll()
            .orderBy(DevicesTable.registeredAt to SortOrder.DESC)
            .map { it.toDevice() }
    }

    fun countOnlineDevices(): Int = transaction(database) {
        countOnline()
    }

    private fun applyInfoUpdate(
        deviceId: String,
        appVersion: String?,
        online: Boolean?,
        remoteAddress: String?,
        now: Long,
        publishChange: Boolean = true,
    ) {
        DevicesTable.update({ DevicesTable.id eq deviceId }) {
            if (appVersion != null) {
                it[DevicesTable.appVersion] = appVersion
            }
            it[DevicesTable.lastSeenAt] = now
            if (online != null) {
                it[DevicesTable.online] = online
            }
            if (remoteAddress != null) {
                it[DevicesTable.remoteAddress] = remoteAddress
            }
        }
        if (publishChange) {
            publish(
                DeviceChangeEvent.DeviceUpdated(
                    deviceId = deviceId,
                    onlineDeviceCount = countOnline(),
                ),
            )
        }
    }

    private fun find(id: String): Device? =
        DevicesTable.selectAll()
            .where { DevicesTable.id eq id }
            .map { it.toDevice() }
            .singleOrNull()

    private fun countOnline(): Int =
        DevicesTable.selectAll()
            .where { DevicesTable.online eq true }
            .count()
            .toInt()

    private fun publish(event: DeviceChangeEvent) {
        changeBus?.publish(event)
    }

    private fun ResultRow.toDevice(): Device =
        Device(
            id = this[DevicesTable.id],
            name = this[DevicesTable.name],
            platform = this[DevicesTable.platform],
            registeredAt = this[DevicesTable.registeredAt],
        )
}
