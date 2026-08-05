package com.dbg.mdm_serverapp.server.data.local.db

import com.dbg.mdm_serverapp.data.network.DeviceChangeBus
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.sql.Connection

class MdmDatabase(
    dbFile: File,
    private val changeBus: DeviceChangeBus? = null,
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

    fun snapshot(lanAddress: String): DeviceChangeEvent.Snapshot =
        DeviceChangeEvent.Snapshot(
            lanAddress = lanAddress,
            devices = devices.list(),
            onlineDeviceCount = devices.countOnlineDevices(),
        )

    fun publishDeviceUpdated(deviceId: String) {
        changeBus?.publish(
            DeviceChangeEvent.DeviceUpdated(
                deviceId = deviceId,
                onlineDeviceCount = devices.countOnlineDevices(),
            ),
        )
    }

    fun getDeviceDetail(deviceId: String): DeviceDetail? {
        val device = devices.get(deviceId) ?: return null
        val info = devices.getInfo(deviceId) ?: return null
        return DeviceDetail(
            device = device,
            info = info,
            facts = deviceFacts.list(deviceId),
        )
    }

    fun close() {
        TransactionManager.closeAndUnregister(database)
    }
}
