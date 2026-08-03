package com.dbg.mdm_serverapp.server.db

import com.dbg.mdm_serverapp.server.model.DeviceFact
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

class DeviceFactsDao(
    private val database: Database,
) {
    fun get(deviceId: String, key: String): DeviceFact? = transaction(database) {
        DeviceFactsTable.selectAll()
            .where { (DeviceFactsTable.deviceId eq deviceId) and (DeviceFactsTable.key eq key) }
            .map { it.toFact() }
            .singleOrNull()
    }

    fun list(deviceId: String): List<DeviceFact> = transaction(database) {
        DeviceFactsTable.selectAll()
            .where { DeviceFactsTable.deviceId eq deviceId }
            .orderBy(DeviceFactsTable.key to SortOrder.ASC)
            .map { it.toFact() }
    }

    fun listAsMap(deviceId: String): Map<String, String> =
        list(deviceId).associate { it.key to it.value }

    /**
     * Inserts or updates a single fact. Missing keys on other rows are left untouched.
     */
    fun upsert(
        deviceId: String,
        key: String,
        value: String,
        now: Long = System.currentTimeMillis(),
    ): DeviceFact = transaction(database) {
        DeviceFactsTable.upsert(DeviceFactsTable.deviceId, DeviceFactsTable.key) {
            it[DeviceFactsTable.deviceId] = deviceId
            it[DeviceFactsTable.key] = key
            it[DeviceFactsTable.value] = value
            it[DeviceFactsTable.updatedAt] = now
        }
        DeviceFact(deviceId = deviceId, key = key, value = value, updatedAt = now)
    }

    /**
     * Merges [facts] into the device's fact set.
     * - Non-null values are upserted
     * - Null values delete that key
     * - Keys not present in [facts] are left unchanged
     */
    fun upsertAll(
        deviceId: String,
        facts: Map<String, String?>,
        now: Long = System.currentTimeMillis(),
    ) {
        transaction(database) {
            for ((key, value) in facts) {
                if (value == null) {
                    DeviceFactsTable.deleteWhere {
                        (DeviceFactsTable.deviceId eq deviceId) and (DeviceFactsTable.key eq key)
                    }
                } else {
                    DeviceFactsTable.upsert(DeviceFactsTable.deviceId, DeviceFactsTable.key) {
                        it[DeviceFactsTable.deviceId] = deviceId
                        it[DeviceFactsTable.key] = key
                        it[DeviceFactsTable.value] = value
                        it[DeviceFactsTable.updatedAt] = now
                    }
                }
            }
        }
    }

    fun delete(deviceId: String, key: String): Boolean = transaction(database) {
        DeviceFactsTable.deleteWhere {
            (DeviceFactsTable.deviceId eq deviceId) and (DeviceFactsTable.key eq key)
        } > 0
    }

    fun deleteAll(deviceId: String): Int = transaction(database) {
        DeviceFactsTable.deleteWhere { DeviceFactsTable.deviceId eq deviceId }
    }

    private fun ResultRow.toFact(): DeviceFact =
        DeviceFact(
            deviceId = this[DeviceFactsTable.deviceId],
            key = this[DeviceFactsTable.key],
            value = this[DeviceFactsTable.value],
            updatedAt = this[DeviceFactsTable.updatedAt],
        )
}
