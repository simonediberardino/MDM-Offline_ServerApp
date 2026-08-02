package com.dbg.mdm_serverapp.server

import com.dbg.mdm_serverapp.api.DbChangeBus
import com.dbg.mdm_serverapp.api.DbChangeEvent
import com.dbg.mdm_serverapp.api.StatusResponse
import com.dbg.mdm_serverapp.server.db.MdmDatabase
import com.dbg.mdm_serverapp.server.dto.RegisterRequest
import com.dbg.mdm_serverapp.server.dto.RegisterResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

private val logger = LoggerFactory.getLogger("MdmServer")

private val eventJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    classDiscriminator = "type"
}

data class MdmServerRuntime(
    val changeBus: DbChangeBus,
    val database: MdmDatabase,
    val httpServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>,
    val discovery: UdpDiscoveryResponder,
) {
    fun snapshot(): DbChangeEvent.Snapshot =
        database.snapshot(LanAddressResolver.primaryIpv4())

    fun shutdown() {
        discovery.stop()
        httpServer.stop(1_000, 2_000)
        database.devices.markAllOffline()
        runCatching { database.close() }
    }
}

fun main() {
    val runtime = startMdmServer(wait = true)
    Runtime.getRuntime().addShutdownHook(Thread { runtime.shutdown() })
}

/**
 * Starts HTTP + UDP discovery, sharing [changeBus] with any in-process UI.
 */
fun startMdmServer(
    changeBus: DbChangeBus = DbChangeBus(),
    wait: Boolean = false,
): MdmServerRuntime {
    val serverId = UUID.randomUUID().toString()
    val dataDir = File(System.getProperty("user.home"), ".mdm_offline")
    val database = MdmDatabase(File(dataDir, "server.db"), changeBus = changeBus)
    database.devices.markAllOffline()

    val lanAddress = LanAddressResolver.primaryIpv4()
    logger.info("MDM Offline server starting on http://$lanAddress:${ServerConfig.HTTP_PORT} (id=$serverId)")

    val discovery = UdpDiscoveryResponder()
    discovery.start()

    val httpServer = embeddedServer(
        factory = Netty,
        port = ServerConfig.HTTP_PORT,
        host = "0.0.0.0",
        module = {
            mdmModule(
                database = database,
                serverId = serverId,
                lanAddressProvider = { LanAddressResolver.primaryIpv4() },
            )
        },
    )
    httpServer.start(wait = wait)

    return MdmServerRuntime(
        changeBus = changeBus,
        database = database,
        httpServer = httpServer,
        discovery = discovery,
    )
}

fun Application.mdmModule(
    database: MdmDatabase,
    serverId: String,
    lanAddressProvider: () -> String,
) {
    install(ContentNegotiation) {
        json(eventJson)
    }

    fun currentStatus(): StatusResponse {
        val snapshot = database.snapshot(lanAddressProvider())
        return StatusResponse(
            running = true,
            lanAddress = snapshot.lanAddress,
            devices = snapshot.devices,
            onlineDeviceCount = snapshot.onlineDeviceCount,
        )
    }

    routing {
        get("/status") {
            call.respond(currentStatus())
        }

        post("/register") {
            val body = call.receive<RegisterRequest>()
            if (body.deviceId.isBlank() || body.deviceName.isBlank() ||
                body.platform.isBlank() || body.appVersion.isBlank()
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    RegisterResponse(
                        serverId = serverId,
                        accepted = false,
                        message = "deviceId, deviceName, platform, and appVersion are required",
                    ),
                )
                return@post
            }

            val remoteAddress = call.request.local.remoteHost
            database.devices.register(
                id = body.deviceId,
                name = body.deviceName,
                platform = body.platform,
                appVersion = body.appVersion,
                online = true,
                remoteAddress = remoteAddress,
            )

            call.respond(
                RegisterResponse(
                    serverId = serverId,
                    accepted = true,
                    message = "welcome",
                ),
            )
        }
    }
}
