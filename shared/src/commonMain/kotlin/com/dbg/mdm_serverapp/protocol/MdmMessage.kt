package com.dbg.mdm_serverapp.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val ProtocolJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

@Serializable
sealed class MdmMessage {
    @Serializable
    @SerialName("HANDSHAKE")
    data class Handshake(
        val deviceId: String,
        val deviceName: String,
        val platform: String,
        val appVersion: String,
    ) : MdmMessage()

    @Serializable
    @SerialName("HANDSHAKE_ACK")
    data class HandshakeAck(
        val serverId: String,
        val accepted: Boolean = true,
        val message: String,
    ) : MdmMessage()

    @Serializable
    @SerialName("EVENT")
    data class Event(
        val id: String,
        val deviceId: String,
        val type: String,
        val payload: String,
        val timestamp: Long,
    ) : MdmMessage()

    @Serializable
    @SerialName("EVENT_ACK")
    data class EventAck(
        val id: String,
        val accepted: Boolean,
    ) : MdmMessage()


    @Serializable
    @SerialName("PING")
    data class Ping(
        val timestamp: Long,
    ) : MdmMessage()

    @Serializable
    @SerialName("PONG")
    data class Pong(
        val timestamp: Long,
    ) : MdmMessage()

    @Serializable
    @SerialName("ERROR")
    data class Error(
        val code: String,
        val message: String,
    ) : MdmMessage()
}

fun encodeMessage(message: MdmMessage): String =
    ProtocolJson.encodeToString(MdmMessage.serializer(), message)

fun decodeMessage(raw: String): MdmMessage =
    ProtocolJson.decodeFromString(MdmMessage.serializer(), raw.trim())
