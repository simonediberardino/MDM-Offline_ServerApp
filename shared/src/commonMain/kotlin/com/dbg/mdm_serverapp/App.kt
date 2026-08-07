package com.dbg.mdm_serverapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.dbg.mdm_serverapp.data.local.AppSettings
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.presentation.i18n.stringsFor
import com.dbg.mdm_serverapp.presentation.navigation.AppRoute
import com.dbg.mdm_serverapp.presentation.ui.DashboardScreen
import com.dbg.mdm_serverapp.presentation.ui.DeviceDetailScreen
import com.dbg.mdm_serverapp.presentation.ui.TutorialScreen
import com.dbg.mdm_serverapp.presentation.ui.theme.WindowsAppTheme
import com.dbg.mdm_serverapp.presentation.viewmodel.DashboardViewModel
import com.dbg.mdm_serverapp.presentation.viewmodel.DeviceDetailViewModel
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
fun App(
    dashboardViewModel: DashboardViewModel,
    deviceRepository: DeviceRepository,
) {
    val settings = remember { AppSettings() }
    val strings = remember { stringsFor(settings.systemLanguage()) }
    val serverState by dashboardViewModel.uiState.collectAsState()

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
                            backStack.add(AppRoute.DeviceDetail(device.id))
                        },
                    )
                }

                entry<AppRoute.DeviceDetail> { route ->
                    val owner = remember(route.deviceId) {
                        object : ViewModelStoreOwner {
                            override val viewModelStore = ViewModelStore()
                        }
                    }
                    DisposableEffect(owner) {
                        onDispose { owner.viewModelStore.clear() }
                    }
                    val detailViewModel = viewModel(viewModelStoreOwner = owner) {
                        DeviceDetailViewModel(
                            deviceRepository = deviceRepository,
                            deviceId = route.deviceId,
                        )
                    }
                    val detail by detailViewModel.detail.collectAsState()
                    val loading by detailViewModel.loading.collectAsState()
                    DeviceDetailScreen(
                        strings = strings,
                        detail = detail,
                        isLoading = loading && detail == null,
                        onBack = { backStack.removeLastOrNull() },
                        onRefresh = { detailViewModel.refresh() },
                    )
                }
            },
        )
    }
}
