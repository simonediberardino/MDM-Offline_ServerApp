package com.dbg.mdm_serverapp.server.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object DeviceFactsTable : Table("device_facts") {
    val deviceId = varchar("device_id", 128)
        .references(DevicesTable.id, onDelete = ReferenceOption.CASCADE)
    val key = varchar("fact_key", 128)
    val value = text("value")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(deviceId, key)
}
