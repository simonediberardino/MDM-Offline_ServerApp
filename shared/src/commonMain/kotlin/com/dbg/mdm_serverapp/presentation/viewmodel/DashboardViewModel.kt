package com.dbg.mdm_serverapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.presentation.state.ServerUiState
import com.dbg.mdm_serverapp.presentation.state.applyDeviceChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Observes [DeviceRepository] and reduces [DeviceChangeEvent]s into [uiState].
 */
class DashboardViewModel(
    private val deviceRepository: DeviceRepository,
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
            _uiState.update { current -> current.applyDeviceChange(deviceRepository.getSnapshot()) }
            deviceRepository.observeChanges().collect { event ->
                _uiState.update { current -> current.applyDeviceChange(event) }
                reloadViewedDetailIfNeeded(event)
            }
        }
    }

    fun refresh() {
        _uiState.update { current -> current.applyDeviceChange(deviceRepository.getSnapshot()) }
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
        _deviceDetail.value = deviceRepository.getDeviceDetail(deviceId)
        _deviceDetailLoading.value = false
    }

    private fun reloadViewedDetailIfNeeded(event: DeviceChangeEvent) {
        val viewed = _viewedDeviceId.value ?: return
        val shouldReload = when (event) {
            is DeviceChangeEvent.Snapshot -> true
            is DeviceChangeEvent.DeviceUpdated -> event.deviceId == viewed
            is DeviceChangeEvent.DeviceRegistered -> event.device.id == viewed
            is DeviceChangeEvent.DevicesMarkedOffline -> true
        }
        if (shouldReload) {
            reloadDeviceDetail(viewed)
        }
    }
}
