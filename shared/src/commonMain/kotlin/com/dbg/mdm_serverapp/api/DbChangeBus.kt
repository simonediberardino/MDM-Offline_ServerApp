package com.dbg.mdm_serverapp.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-process bus of DB change events.
 * Server mutations publish here; [DashboardViewModel] collects [events].
 */
class DbChangeBus {
    private val _events = MutableSharedFlow<DbChangeEvent>(
        extraBufferCapacity = 64,
    )
    val events: SharedFlow<DbChangeEvent> = _events.asSharedFlow()

    fun publish(event: DbChangeEvent) {
        _events.tryEmit(event)
    }
}
