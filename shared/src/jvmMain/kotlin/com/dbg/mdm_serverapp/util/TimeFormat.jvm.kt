package com.dbg.mdm_serverapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatEpoch(epochMs: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(epochMs))
}
