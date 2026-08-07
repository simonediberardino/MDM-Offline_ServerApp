package com.dbg.mdm_serverapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DevicePresence
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.presentation.state.ServerUiState
import com.dbg.mdm_serverapp.presentation.state.applyDeviceChange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DashboardViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServerUiState())
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { current -> current.applyDeviceChange(deviceRepository.getSnapshot()) }
            deviceRepository.observeChanges().collect { event ->
                _uiState.update { current -> current.applyDeviceChange(event) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(DevicePresence.ONLINE_RECOMPUTE_INTERVAL_MS.milliseconds)
                _uiState.update { it.applyDeviceChange(DeviceChangeEvent.PresenceTick) }
            }
        }
    }

    fun refresh() {
        _uiState.update { current -> current.applyDeviceChange(deviceRepository.getSnapshot()) }
    }
}
