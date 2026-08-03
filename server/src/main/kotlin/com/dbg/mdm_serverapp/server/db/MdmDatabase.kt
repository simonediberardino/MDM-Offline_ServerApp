package com.dbg.mdm_serverapp.server.db

import com.dbg.mdm_serverapp.api.DbChangeBus
import com.dbg.mdm_serverapp.api.DbChangeEvent
import com.dbg.mdm_serverapp.api.StatusDeviceDto
import com.dbg.mdm_serverapp.model.Device
import com.dbg.mdm_serverapp.model.DeviceDetail
import com.dbg.mdm_serverapp.model.DeviceFact
import com.dbg.mdm_serverapp.model.DeviceInfo
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.sql.Connection

class MdmDatabase(
    dbFile: File,
    changeBus: DbChangeBus? = null,
) {
    private val database: Database
    val devices: DevicesDao
    val deviceFacts: DeviceFactsDao

    init {
        dbFile.parentFile?.mkdirs()
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC",
            setupConnection = { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("PRAGMA foreign_keys = ON")
                }
            },
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction(database) {
            SchemaUtils.create(DevicesTable, DeviceFactsTable)
        }
        devices = DevicesDao(database, changeBus)
        deviceFacts = DeviceFactsDao(database)
    }

    fun snapshot(lanAddress: String): DbChangeEvent.Snapshot =
        DbChangeEvent.Snapshot(
            lanAddress = lanAddress,
            devices = devices.list().map { device ->
                StatusDeviceDto(
                    id = device.id,
                    name = device.name,
                    platform = device.platform,
                    registeredAt = device.registeredAt,
                )
            },
            onlineDeviceCount = devices.countOnlineDevices(),
        )

    fun getDeviceDetail(deviceId: String): DeviceDetail? {
        val device = devices.get(deviceId) ?: return null
        val info = devices.getInfo(deviceId) ?: return null
        return DeviceDetail(
            device = Device(
                id = device.id,
                name = device.name,
                platform = device.platform,
                registeredAt = device.registeredAt,
            ),
            info = DeviceInfo(
                deviceId = info.deviceId,
                appVersion = info.appVersion,
                lastSeenAt = info.lastSeenAt,
                online = info.online,
                remoteAddress = info.remoteAddress,
            ),
            facts = deviceFacts.list(deviceId).map { fact ->
                DeviceFact(
                    deviceId = fact.deviceId,
                    key = fact.key,
                    value = fact.value,
                    updatedAt = fact.updatedAt,
                )
            },
        )
    }

    fun close() {
        TransactionManager.closeAndUnregister(database)
    }
}
