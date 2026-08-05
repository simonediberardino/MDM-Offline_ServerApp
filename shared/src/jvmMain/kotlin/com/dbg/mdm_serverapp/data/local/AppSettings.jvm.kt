package com.dbg.mdm_serverapp.data.local

import com.dbg.mdm_serverapp.domain.model.AppLanguage
import java.util.Locale
import java.util.prefs.Preferences

actual class AppSettings actual constructor() {
    private val prefs = Preferences.userRoot().node("com.dbg.mdm_offline")

    actual var tutorialCompleted: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false)
        set(value) = prefs.putBoolean(KEY_TUTORIAL_COMPLETED, value)

    actual fun systemLanguage(): AppLanguage =
        AppLanguage.fromLocaleTag(Locale.getDefault().toLanguageTag())

    init {
        // Drop any previously saved manual language preference.
        prefs.remove(KEY_LANGUAGE)
    }

    companion object {
        private const val KEY_TUTORIAL_COMPLETED = "tutorial_completed"
        private const val KEY_LANGUAGE = "language"
    }
}
