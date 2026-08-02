package com.dbg.mdm_serverapp.server.db

import org.jetbrains.exposed.v1.core.Table

object DevicesTable : Table("devices") {
    val id = varchar("id", 128)
    val name = varchar("name", 256)
    val platform = varchar("platform", 64)
    val registeredAt = long("registered_at")
    val appVersion = varchar("app_version", 64)
    val lastSeenAt = long("last_seen_at")
    val online = bool("online").default(false)
    val remoteAddress = varchar("remote_address", 128).nullable()

    override val primaryKey = PrimaryKey(id)
}
