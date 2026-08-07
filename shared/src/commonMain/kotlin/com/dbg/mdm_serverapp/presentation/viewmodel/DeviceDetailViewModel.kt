package com.dbg.mdm_serverapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbg.mdm_serverapp.domain.event.DeviceChangeEvent
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import com.dbg.mdm_serverapp.domain.model.DevicePresence
import com.dbg.mdm_serverapp.domain.repository.DeviceRepository
import com.dbg.mdm_serverapp.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Single-device detail. Created when entering the screen; cancelled when leaving,
 * so periodic refresh only runs while this screen is shown.
 */
class DeviceDetailViewModel(
    private val deviceRepository: DeviceRepository,
    private val deviceId: String,
) : ViewModel() {
    private val _detail = MutableStateFlow<DeviceDetail?>(null)
    val detail: StateFlow<DeviceDetail?> = _detail.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            deviceRepository.observeChanges().collect { event ->
                if (affectsThisDevice(event)) {
                    refresh(showLoading = false)
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(DevicePresence.ONLINE_RECOMPUTE_INTERVAL_MS.milliseconds)
                _detail.update { current ->
                    current?.let {
                        val online = DevicePresence.isOnline(it.info.lastSeenAt, currentTimeMillis())
                        it.copy(info = it.info.copy(online = online))
                    }
                }
            }
        }
    }

    fun refresh(showLoading: Boolean = true) {
        if (showLoading) {
            _loading.value = true
        }
        _detail.value = deviceRepository.getDeviceDetail(deviceId)
        _loading.value = false
    }

    private fun affectsThisDevice(event: DeviceChangeEvent): Boolean =
        when (event) {
            is DeviceChangeEvent.Snapshot -> true
            is DeviceChangeEvent.DeviceUpdated -> event.deviceId == deviceId
            is DeviceChangeEvent.DeviceRegistered -> event.device.id == deviceId
            is DeviceChangeEvent.PresenceTick -> false
        }
}
