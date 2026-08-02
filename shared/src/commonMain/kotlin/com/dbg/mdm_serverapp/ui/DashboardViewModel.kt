package com.dbg.mdm_serverapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbg.mdm_serverapp.api.DbChangeBus
import com.dbg.mdm_serverapp.api.DbChangeEvent
import com.dbg.mdm_serverapp.api.ServerUiState
import com.dbg.mdm_serverapp.api.applyDbChange
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServerUiState())
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { current -> current.applyDbChange(snapshotProvider()) }
            changeBus.events.collect { event ->
                _uiState.update { current -> current.applyDbChange(event) }
            }
        }
    }

    fun refresh() {
        _uiState.update { current -> current.applyDbChange(snapshotProvider()) }
    }
}
