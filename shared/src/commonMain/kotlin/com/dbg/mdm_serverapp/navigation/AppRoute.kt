package com.dbg.mdm_serverapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Tutorial : AppRoute

    @Serializable
    data object Devices : AppRoute

    @Serializable
    data class DeviceDetail(val deviceId: String) : AppRoute
}
