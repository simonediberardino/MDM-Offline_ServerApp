package com.dbg.mdm_serverapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dbg.mdm_serverapp.i18n.stringsFor
import com.dbg.mdm_serverapp.settings.AppSettings
import com.dbg.mdm_serverapp.ui.DashboardScreen
import com.dbg.mdm_serverapp.ui.DashboardViewModel
import com.dbg.mdm_serverapp.ui.TutorialScreen
import com.dbg.mdm_serverapp.ui.theme.WindowsAppTheme

@Composable
fun App(dashboardViewModel: DashboardViewModel) {
    val settings = remember { AppSettings() }
    val strings = remember { stringsFor(settings.systemLanguage()) }
    var showTutorial by remember { mutableStateOf(!settings.tutorialCompleted) }
    val serverState by dashboardViewModel.uiState.collectAsState()

    WindowsAppTheme {
        if (showTutorial) {
            TutorialScreen(
                strings = strings,
                onFinished = {
                    settings.tutorialCompleted = true
                    showTutorial = false
                },
            )
        } else {
            DashboardScreen(
                strings = strings,
                running = serverState.running,
                lanAddress = serverState.lanAddress.ifBlank { "—" },
                devices = serverState.devices,
                onlineDeviceCount = serverState.onlineDeviceCount,
                onRefresh = { dashboardViewModel.refresh() },
                onShowTutorial = { showTutorial = true },
            )
        }
    }
}
