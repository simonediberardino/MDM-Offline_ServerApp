package com.dbg.mdm_serverapp.server

import com.dbg.mdm_serverapp.domain.model.DevicePresence
import com.dbg.mdm_serverapp.server.data.local.db.MdmDatabase
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MdmDatabaseTest {

    @Test
    fun databaseStoresDevicesAndDeviceInfosSeparately() {
        val tempFile = Files.createTempFile("mdm-test", ".db").toFile()
        tempFile.deleteOnExit()
        val database = MdmDatabase(tempFile)
        val devices = database.devices

        devices.register(
            id = "d1",
            name = "Tablet A",
            platform = "Android",
            appVersion = "1.0.0",
            remoteAddress = "192.168.1.20",
        )
        devices.register(
            id = "d1",
            name = "Should Not Change",
            platform = "Should Not Change",
            appVersion = "2.0.0",
            remoteAddress = "192.168.1.21",
        )

        val listed = devices.list()
        assertEquals(1, listed.size)
        val device = listed.first()
        assertEquals("Tablet A", device.name)
        assertEquals("Android", device.platform)
        assertTrue(device.online)

        val info = devices.getInfo("d1")!!
        assertEquals("2.0.0", info.appVersion)
        assertEquals("192.168.1.21", info.remoteAddress)
        assertTrue(info.online)
        assertEquals(1, devices.countOnlineDevices())

        val staleNow = info.lastSeenAt + DevicePresence.ONLINE_THRESHOLD_MS
        assertFalse(devices.getInfo("d1", now = staleNow)!!.online)
        assertEquals(0, devices.countOnlineDevices(now = staleNow))

        database.close()
    }

    @Test
    fun deviceFactsSupportSparseMergeAndDelete() {
        val tempFile = Files.createTempFile("mdm-facts-test", ".db").toFile()
        tempFile.deleteOnExit()
        val database = MdmDatabase(tempFile)

        database.devices.register(
            id = "d1",
            name = "Tablet A",
            platform = "Android",
            appVersion = "1.0.0",
            remoteAddress = "192.168.1.20",
        )

        val facts = database.deviceFacts
        facts.upsert("d1", "battery_level", "87")
        facts.upsertAll(
            deviceId = "d1",
            facts = mapOf(
                "battery_level" to "80",
                "os_build" to "14",
                "imei" to "123",
            ),
        )

        assertEquals("80", facts.get("d1", "battery_level")!!.value)
        assertEquals(
            mapOf("battery_level" to "80", "imei" to "123", "os_build" to "14"),
            facts.listAsMap("d1"),
        )

        facts.upsertAll(
            deviceId = "d1",
            facts = mapOf(
                "imei" to null,
                "battery_level" to "79",
            ),
        )
        assertNull(facts.get("d1", "imei"))
        assertEquals("79", facts.get("d1", "battery_level")!!.value)
        assertEquals("14", facts.get("d1", "os_build")!!.value)

        assertTrue(facts.delete("d1", "os_build"))
        assertEquals(1, facts.deleteAll("d1"))
        assertTrue(facts.list("d1").isEmpty())

        database.close()
    }

    @Test
    fun getDeviceDetailIncludesIdentityInfoAndFacts() {
        val tempFile = Files.createTempFile("mdm-detail-test", ".db").toFile()
        tempFile.deleteOnExit()
        val database = MdmDatabase(tempFile)

        database.devices.register(
            id = "d1",
            name = "Tablet A",
            platform = "Android",
            appVersion = "1.0.0",
            remoteAddress = "192.168.1.20",
        )
        database.deviceFacts.upsertAll(
            deviceId = "d1",
            facts = mapOf("battery_level" to "55"),
        )

        val detail = database.getDeviceDetail("d1")!!
        assertEquals("Tablet A", detail.device.name)
        assertEquals("1.0.0", detail.info.appVersion)
        assertEquals("192.168.1.20", detail.info.remoteAddress)
        assertTrue(detail.info.online)
        assertEquals(1, detail.facts.size)
        assertEquals("battery_level", detail.facts.first().key)
        assertEquals("55", detail.facts.first().value)

        database.close()
    }
}
