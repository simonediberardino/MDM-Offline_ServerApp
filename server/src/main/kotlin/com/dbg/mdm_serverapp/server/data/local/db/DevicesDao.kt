package com.dbg.mdm_serverapp.server.data.local.db

import com.dbg.mdm_serverapp.data.network.DeviceChangeBus
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.Device
import com.dbg.mdm_serverapp.domain.model.DeviceInfo
import com.dbg.mdm_serverapp.domain.model.DevicePresence
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
        remoteAddress: String?,
        now: Long = System.currentTimeMillis(),
    ): Device = transaction(database) {
        val existing = find(id, now)
        if (existing == null) {
            DevicesTable.insert {
                it[DevicesTable.id] = id
                it[DevicesTable.name] = name
                it[DevicesTable.platform] = platform
                it[DevicesTable.registeredAt] = now
                it[DevicesTable.appVersion] = appVersion
                it[DevicesTable.lastSeenAt] = now
                it[DevicesTable.remoteAddress] = remoteAddress
            }
            val device = find(id, now)!!
            publish(
                DeviceChangeEvent.DeviceRegistered(
                    device = device,
                    onlineDeviceCount = countOnline(now),
                ),
            )
            device
        } else {
            applyInfoUpdate(
                deviceId = id,
                appVersion = appVersion,
                remoteAddress = remoteAddress,
                now = now,
            )
            find(id, now)!!
        }
    }

    fun updateInfo(
        deviceId: String,
        appVersion: String? = null,
        remoteAddress: String? = null,
        now: Long = System.currentTimeMillis(),
        publishChange: Boolean = true,
    ) {
        transaction(database) {
            applyInfoUpdate(
                deviceId = deviceId,
                appVersion = appVersion,
                remoteAddress = remoteAddress,
                now = now,
                publishChange = publishChange,
            )
        }
    }

    fun get(id: String, now: Long = System.currentTimeMillis()): Device? = transaction(database) {
        find(id, now)
    }

    fun getInfo(deviceId: String, now: Long = System.currentTimeMillis()): DeviceInfo? =
        transaction(database) {
            DevicesTable.selectAll()
                .where { DevicesTable.id eq deviceId }
                .map { row ->
                    val lastSeenAt = row[DevicesTable.lastSeenAt]
                    DeviceInfo(
                        deviceId = row[DevicesTable.id],
                        appVersion = row[DevicesTable.appVersion],
                        lastSeenAt = lastSeenAt,
                        online = DevicePresence.isOnline(lastSeenAt, now),
                        remoteAddress = row[DevicesTable.remoteAddress],
                    )
                }
                .singleOrNull()
        }

    fun list(now: Long = System.currentTimeMillis()): List<Device> = transaction(database) {
        DevicesTable.selectAll()
            .orderBy(DevicesTable.registeredAt to SortOrder.DESC)
            .map { it.toDevice(now) }
    }

    fun countOnlineDevices(now: Long = System.currentTimeMillis()): Int = transaction(database) {
        countOnline(now)
    }

    private fun applyInfoUpdate(
        deviceId: String,
        appVersion: String?,
        remoteAddress: String?,
        now: Long,
        publishChange: Boolean = true,
    ) {
        DevicesTable.update({ DevicesTable.id eq deviceId }) {
            if (appVersion != null) {
                it[DevicesTable.appVersion] = appVersion
            }
            it[DevicesTable.lastSeenAt] = now
            if (remoteAddress != null) {
                it[DevicesTable.remoteAddress] = remoteAddress
            }
        }
        if (publishChange) {
            publish(
                DeviceChangeEvent.DeviceUpdated(
                    deviceId = deviceId,
                    lastSeenAt = now,
                    onlineDeviceCount = countOnline(now),
                ),
            )
        }
    }

    private fun find(id: String, now: Long): Device? =
        DevicesTable.selectAll()
            .where { DevicesTable.id eq id }
            .map { it.toDevice(now) }
            .singleOrNull()

    private fun countOnline(now: Long): Int =
        DevicesTable.selectAll()
            .count { row -> DevicePresence.isOnline(row[DevicesTable.lastSeenAt], now) }

    private fun publish(event: DeviceChangeEvent) {
        changeBus?.publish(event)
    }

    private fun ResultRow.toDevice(now: Long): Device {
        val lastSeenAt = this[DevicesTable.lastSeenAt]
        return Device(
            id = this[DevicesTable.id],
            name = this[DevicesTable.name],
            platform = this[DevicesTable.platform],
            registeredAt = this[DevicesTable.registeredAt],
            lastSeenAt = lastSeenAt,
            online = DevicePresence.isOnline(lastSeenAt, now),
        )
    }
}
