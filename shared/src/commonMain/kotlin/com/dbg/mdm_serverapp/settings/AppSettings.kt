package com.dbg.mdm_serverapp.settings

import com.dbg.mdm_serverapp.model.AppLanguage

expect class AppSettings() {
    var tutorialCompleted: Boolean

    /** Always follows the device/OS locale. Unsupported languages fall back to English. */
    fun systemLanguage(): AppLanguage
}
