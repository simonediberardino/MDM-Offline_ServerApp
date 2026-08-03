package com.dbg.mdm_serverapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.dbg.mdm_serverapp.i18n.stringsFor
import com.dbg.mdm_serverapp.navigation.AppRoute
import com.dbg.mdm_serverapp.settings.AppSettings
import com.dbg.mdm_serverapp.ui.DashboardScreen
import com.dbg.mdm_serverapp.ui.DashboardViewModel
import com.dbg.mdm_serverapp.ui.DeviceDetailScreen
import com.dbg.mdm_serverapp.ui.TutorialScreen
import com.dbg.mdm_serverapp.ui.theme.WindowsAppTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Tutorial::class, AppRoute.Tutorial.serializer())
            subclass(AppRoute.Devices::class, AppRoute.Devices.serializer())
            subclass(AppRoute.DeviceDetail::class, AppRoute.DeviceDetail.serializer())
        }
    }
}

@Composable
fun App(dashboardViewModel: DashboardViewModel) {
    val settings = remember { AppSettings() }
    val strings = remember { stringsFor(settings.systemLanguage()) }
    val serverState by dashboardViewModel.uiState.collectAsState()
    val deviceDetail by dashboardViewModel.deviceDetail.collectAsState()
    val deviceDetailLoading by dashboardViewModel.deviceDetailLoading.collectAsState()
    val viewedDeviceId by dashboardViewModel.viewedDeviceId.collectAsState()

    val startRoute = if (settings.tutorialCompleted) AppRoute.Devices else AppRoute.Tutorial
    val backStack = rememberNavBackStack(navConfig, startRoute)

    WindowsAppTheme {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = entryProvider {
                entry<AppRoute.Tutorial> {
                    TutorialScreen(
                        strings = strings,
                        onFinished = {
                            settings.tutorialCompleted = true
                            backStack.clear()
                            backStack.add(AppRoute.Devices)
                        },
                    )
                }

                entry<AppRoute.Devices> {
                    DashboardScreen(
                        strings = strings,
                        running = serverState.running,
                        lanAddress = serverState.lanAddress.ifBlank { "—" },
                        devices = serverState.devices,
                        onlineDeviceCount = serverState.onlineDeviceCount,
                        onRefresh = { dashboardViewModel.refresh() },
                        onShowTutorial = { backStack.add(AppRoute.Tutorial) },
                        onDeviceClick = { device ->
                            dashboardViewModel.loadDeviceDetail(device.id)
                            backStack.add(AppRoute.DeviceDetail(device.id))
                        },
                    )
                }

                entry<AppRoute.DeviceDetail> { route ->
                    LaunchedEffect(route.deviceId) {
                        dashboardViewModel.loadDeviceDetail(route.deviceId)
                    }
                    DisposableEffect(route.deviceId) {
                        onDispose { dashboardViewModel.clearDeviceDetail() }
                    }
                    val detailReady = viewedDeviceId == route.deviceId && !deviceDetailLoading
                    DeviceDetailScreen(
                        strings = strings,
                        detail = deviceDetail.takeIf { detailReady },
                        isLoading = !detailReady,
                        onBack = { backStack.removeLastOrNull() },
                        onRefresh = { dashboardViewModel.refresh() },
                    )
                }
            },
        )
    }
}
