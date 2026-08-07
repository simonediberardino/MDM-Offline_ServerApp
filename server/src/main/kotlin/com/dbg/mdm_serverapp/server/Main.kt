package com.dbg.mdm_serverapp.server

import com.dbg.mdm_serverapp.data.network.DeviceChangeBus
import com.dbg.mdm_serverapp.data.network.dto.RegisterRequest
import com.dbg.mdm_serverapp.data.network.dto.RegisterResponse
import com.dbg.mdm_serverapp.data.network.dto.StatusResponse
import com.dbg.mdm_serverapp.data.network.dto.UpdateInfoRequest
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.server.data.DeviceRepositoryImpl
import com.dbg.mdm_serverapp.server.data.local.db.MdmDatabase
import com.dbg.mdm_serverapp.server.data.network.UdpDiscoveryResponder
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
    val deviceRepository: DeviceRepository,
    val database: MdmDatabase,
    val httpServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>,
    val discovery: UdpDiscoveryResponder,
) {
    fun shutdown() {
        discovery.stop()
        httpServer.stop(1_000, 2_000)
        runCatching { database.close() }
    }
}

/**
 * Starts HTTP + UDP discovery, exposing a [DeviceRepository] for any in-process UI.
 */
fun startMdmServer(
    changeBus: DeviceChangeBus = DeviceChangeBus(),
    wait: Boolean = false,
): MdmServerRuntime {
    val serverId = UUID.randomUUID().toString()
    val dataDir = File(System.getProperty("user.home"), ".mdm_offline")
    val database = MdmDatabase(File(dataDir, "server.db"), changeBus = changeBus)

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

    val deviceRepository = DeviceRepositoryImpl(
        database = database,
        changeBus = changeBus,
        lanAddressProvider = { LanAddressResolver.primaryIpv4() },
    )

    return MdmServerRuntime(
        deviceRepository = deviceRepository,
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
        )
    }

    routing {
        get("/status") {
            val deviceId = call.request.queryParameters["deviceId"]?.takeIf { it.isNotBlank() }
            logger.info("Executing status... $deviceId")

            if (deviceId != null && database.devices.get(deviceId) != null) {
                logger.info("Received status from deviceId=$deviceId address:${call.request.local.remoteHost}")
                database.devices.updateInfo(
                    deviceId = deviceId,
                    remoteAddress = call.request.local.remoteHost,
                )
            }
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

        post("/update_info") {
            val body = call.receive<UpdateInfoRequest>()
            if (body.deviceId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            if (database.devices.get(body.deviceId) == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val remoteAddress = call.request.local.remoteHost
            val now = System.currentTimeMillis()
            database.devices.updateInfo(
                deviceId = body.deviceId,
                appVersion = body.appVersion?.takeIf { it.isNotBlank() },
                remoteAddress = remoteAddress,
                now = now,
                publishChange = false,
            )
            if (body.facts.isNotEmpty()) {
                database.deviceFacts.upsertAll(
                    deviceId = body.deviceId,
                    facts = body.facts,
                )
            }
            database.publishDeviceUpdated(body.deviceId, lastSeenAt = now)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
