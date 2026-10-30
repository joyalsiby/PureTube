package com.puretube.player.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    DARK("Dark Theme"),
    LIGHT("Light Theme")
}

enum class KidsUnlockMode(val label: String, val description: String) {
    HOLD_2S("Hold for 2 Seconds (Recommended)", "Hold the unlock pill for 2 seconds to prevent accidental toddler taps"),
    MATH_CHALLENGE("Parent Math Challenge", "Solve a quick multiplication question (e.g. 6 × 4 = ?) to unlock"),
    SINGLE_TAP("Single Tap", "Unlock immediately with a single tap")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val defaultQualityHeight: Int = 1080, // 2160 (4K), 1080, 720, 480, 360
    val defaultPlaybackSpeed: Float = 1.0f,
    val backgroundPlayEnabled: Boolean = false, // Disabled by default; PiP handles leaving app while playing
    val autoPipOnHome: Boolean = true,
    // Kids / Focus Mode Settings
    val autoStartKidsLocked: Boolean = false, // Start videos with controls locked for toddlers/kids
    val kidsUnlockMode: KidsUnlockMode = KidsUnlockMode.HOLD_2S,
    val autoFullscreenOnPlay: Boolean = false, // Open videos directly in fullscreen landscape
    val hideVideoDescription: Boolean = false, // Hide description & stats below player for zero distraction
    val saveWatchHistory: Boolean = true,
    // Updater & Onboarding / Release Notes State
    val autoCheckUpdates: Boolean = true,
    val skippedUpdateVersion: String = "",
    val onboardingCompleted: Boolean = false,
    val lastSeenVersion: String = "",
    val cachedReleaseNotesVersion: String = "",
    val cachedReleaseNotesBody: String = ""
)

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("puretube_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val unlockStr = prefs.getString("kids_unlock_mode", KidsUnlockMode.HOLD_2S.name) ?: KidsUnlockMode.HOLD_2S.name
        val kidsUnlockMode = runCatching { KidsUnlockMode.valueOf(unlockStr) }.getOrDefault(KidsUnlockMode.HOLD_2S)

        return AppSettings(
            themeMode = themeMode,
            dynamicColorEnabled = prefs.getBoolean("dynamic_color", true),
            defaultQualityHeight = prefs.getInt("default_quality_height", 1080),
            defaultPlaybackSpeed = prefs.getFloat("default_playback_speed", 1.0f),
            backgroundPlayEnabled = prefs.getBoolean("background_play_v2", false),
            autoPipOnHome = prefs.getBoolean("auto_pip_on_home", true),
            autoStartKidsLocked = prefs.getBoolean("auto_start_kids_locked", false),
            kidsUnlockMode = kidsUnlockMode,
            autoFullscreenOnPlay = prefs.getBoolean("auto_fullscreen_on_play", false),
            hideVideoDescription = prefs.getBoolean("hide_video_description", false),
            saveWatchHistory = prefs.getBoolean("save_watch_history", true),
            autoCheckUpdates = prefs.getBoolean("auto_check_updates", true),
            skippedUpdateVersion = prefs.getString("skipped_update_version", "").orEmpty(),
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            lastSeenVersion = prefs.getString("last_seen_version", "").orEmpty(),
            cachedReleaseNotesVersion = prefs.getString("cached_release_notes_version", "").orEmpty(),
            cachedReleaseNotesBody = prefs.getString("cached_release_notes_body", "").orEmpty()
        )
    }

    private fun update(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        _settings.value = loadSettings()
    }

    fun setThemeMode(mode: AppThemeMode) = update { putString("theme_mode", mode.name) }
    fun setDynamicColorEnabled(enabled: Boolean) = update { putBoolean("dynamic_color", enabled) }
    fun setDefaultQualityHeight(height: Int) = update { putInt("default_quality_height", height) }
    fun setDefaultPlaybackSpeed(speed: Float) = update { putFloat("default_playback_speed", speed) }
    fun setBackgroundPlayEnabled(enabled: Boolean) = update { putBoolean("background_play_v2", enabled) }
    fun setAutoPipOnHome(enabled: Boolean) = update { putBoolean("auto_pip_on_home", enabled) }
    fun setAutoStartKidsLocked(enabled: Boolean) = update { putBoolean("auto_start_kids_locked", enabled) }
    fun setKidsUnlockMode(mode: KidsUnlockMode) = update { putString("kids_unlock_mode", mode.name) }
    fun setAutoFullscreenOnPlay(enabled: Boolean) = update { putBoolean("auto_fullscreen_on_play", enabled) }
    fun setHideVideoDescription(enabled: Boolean) = update { putBoolean("hide_video_description", false.takeIf { !enabled } ?: true) }
    fun setSaveWatchHistory(enabled: Boolean) = update { putBoolean("save_watch_history", enabled) }
    fun setAutoCheckUpdates(enabled: Boolean) = update { putBoolean("auto_check_updates", enabled) }
    fun setSkippedUpdateVersion(version: String) = update { putString("skipped_update_version", version) }
    fun setOnboardingCompleted(completed: Boolean) = update { putBoolean("onboarding_completed", completed) }
    fun setLastSeenVersion(version: String) = update { putString("last_seen_version", version) }
    fun setCachedReleaseNotes(version: String, body: String) = update {
        putString("cached_release_notes_version", version)
        putString("cached_release_notes_body", body)
    }
}
