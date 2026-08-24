package com.dbg.mdm_serverapp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dbg.mdm_serverapp.presentation.viewmodel.DashboardViewModel
import com.dbg.mdm_serverapp.server.startMdmServer
import kotlin.system.exitProcess

fun main() {
    val runtime = startMdmServer(wait = false)
    val dashboardViewModel = DashboardViewModel(
        deviceRepository = runtime.deviceRepository,
    )
    Runtime.getRuntime().addShutdownHook(Thread { runtime.shutdown() })

    application {
        Window(
            onCloseRequest = { exitProcess(0) },
            title = "MDM Offline",
            state = rememberWindowState(width = 1180.dp, height = 780.dp),
        ) {
            App(
                dashboardViewModel = dashboardViewModel,
                deviceRepository = runtime.deviceRepository,
            )
        }
    }
}