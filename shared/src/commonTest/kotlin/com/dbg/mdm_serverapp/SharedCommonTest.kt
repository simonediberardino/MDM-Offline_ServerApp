package com.dbg.mdm_serverapp

import com.dbg.mdm_serverapp.model.AppLanguage
import com.dbg.mdm_serverapp.protocol.MdmMessage
import com.dbg.mdm_serverapp.protocol.ProtocolConstants
import com.dbg.mdm_serverapp.protocol.buildDiscoverReply
import com.dbg.mdm_serverapp.protocol.decodeMessage
import com.dbg.mdm_serverapp.protocol.encodeMessage
import com.dbg.mdm_serverapp.protocol.parseDiscoverReply
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun httpPortIsDefined() {
        assertEquals(9876, ProtocolConstants.HTTP_PORT)
        assertEquals(9877, ProtocolConstants.UDP_PORT)
    }

    @Test
    fun discoverReplyRoundTrip() {
        val raw = buildDiscoverReply("192.168.1.10", 9876)
        assertEquals("MDM_SERVER|192.168.1.10|9876", raw)
        val parsed = parseDiscoverReply(raw)!!
        assertEquals("192.168.1.10", parsed.localIp)
        assertEquals(9876, parsed.httpPort)
    }

    @Test
    fun handshakeRoundTrip() {
        val original = MdmMessage.Handshake(
            deviceId = "device-1",
            deviceName = "Pixel",
            platform = "Android",
            appVersion = "1.0.0",
        )
        val encoded = encodeMessage(original)
        val decoded = decodeMessage(encoded)
        assertTrue(decoded is MdmMessage.Handshake)
        val handshake = decoded as MdmMessage.Handshake
        assertEquals("device-1", handshake.deviceId)
        assertEquals("Pixel", handshake.deviceName)
    }

    @Test
    fun localeTagMapsToSupportedLanguageOrEnglish() {
        assertEquals(AppLanguage.ITALIAN, AppLanguage.fromLocaleTag("it-IT"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocaleTag("en-US"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocaleTag("fr-FR"))
    }
}
