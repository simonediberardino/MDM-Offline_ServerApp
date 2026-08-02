package com.dbg.mdm_serverapp.server.db

import com.dbg.mdm_serverapp.api.DbChangeBus
import com.dbg.mdm_serverapp.api.DbChangeEvent
import com.dbg.mdm_serverapp.api.StatusDeviceDto
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
            SchemaUtils.create(DevicesTable)
        }
        devices = DevicesDao(database, changeBus)
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

    fun close() {
        TransactionManager.closeAndUnregister(database)
    }
}
