package com.dbg.mdm_serverapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbg.mdm_serverapp.api.DbChangeBus
import com.dbg.mdm_serverapp.api.DbChangeEvent
import com.dbg.mdm_serverapp.api.ServerUiState
import com.dbg.mdm_serverapp.api.applyDbChange
import com.dbg.mdm_serverapp.model.DeviceDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Listens to [DbChangeBus.events] and reduces them into [uiState].
 */
class DashboardViewModel(
    private val changeBus: DbChangeBus,
    private val snapshotProvider: () -> DbChangeEvent.Snapshot,
    private val deviceDetailProvider: (String) -> DeviceDetail?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServerUiState())
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    private val _viewedDeviceId = MutableStateFlow<String?>(null)
    val viewedDeviceId: StateFlow<String?> = _viewedDeviceId.asStateFlow()

    private val _deviceDetail = MutableStateFlow<DeviceDetail?>(null)
    val deviceDetail: StateFlow<DeviceDetail?> = _deviceDetail.asStateFlow()

    private val _deviceDetailLoading = MutableStateFlow(false)
    val deviceDetailLoading: StateFlow<Boolean> = _deviceDetailLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { current -> current.applyDbChange(snapshotProvider()) }
            changeBus.events.collect { event ->
                _uiState.update { current -> current.applyDbChange(event) }
                reloadViewedDetailIfNeeded(event)
            }
        }
    }

    fun refresh() {
        _uiState.update { current -> current.applyDbChange(snapshotProvider()) }
        _viewedDeviceId.value?.let { id ->
            reloadDeviceDetail(id)
        }
    }

    fun loadDeviceDetail(deviceId: String) {
        _viewedDeviceId.value = deviceId
        reloadDeviceDetail(deviceId)
    }

    fun clearDeviceDetail() {
        _viewedDeviceId.value = null
        _deviceDetail.value = null
        _deviceDetailLoading.value = false
    }

    private fun reloadDeviceDetail(deviceId: String) {
        _deviceDetailLoading.value = true
        _deviceDetail.value = deviceDetailProvider(deviceId)
        _deviceDetailLoading.value = false
    }

    private fun reloadViewedDetailIfNeeded(event: DbChangeEvent) {
        val viewed = _viewedDeviceId.value ?: return
        val shouldReload = when (event) {
            is DbChangeEvent.Snapshot -> true
            is DbChangeEvent.DeviceUpdated -> event.deviceId == viewed
            is DbChangeEvent.DeviceRegistered -> event.device.id == viewed
            is DbChangeEvent.DevicesMarkedOffline -> true
        }
        if (shouldReload) {
            reloadDeviceDetail(viewed)
        }
    }
}
