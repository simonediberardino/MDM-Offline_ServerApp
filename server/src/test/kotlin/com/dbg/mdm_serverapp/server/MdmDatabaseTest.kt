package com.dbg.mdm_serverapp.server

import com.dbg.mdm_serverapp.server.db.MdmDatabase
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
            online = true,
            remoteAddress = "192.168.1.20",
        )
        devices.register(
            id = "d1",
            name = "Should Not Change",
            platform = "Should Not Change",
            appVersion = "2.0.0",
            online = true,
            remoteAddress = "192.168.1.21",
        )

        val listed = devices.list()
        assertEquals(1, listed.size)
        val device = listed.first()
        assertEquals("Tablet A", device.name)
        assertEquals("Android", device.platform)

        val info = devices.getInfo("d1")!!
        assertEquals("2.0.0", info.appVersion)
        assertEquals("192.168.1.21", info.remoteAddress)
        assertTrue(info.online)
        assertEquals(1, devices.countOnlineDevices())

        devices.markAllOffline()
        assertFalse(devices.getInfo("d1")!!.online)
        assertEquals(0, devices.countOnlineDevices())

        database.close()
    }
}
