package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AmbientSoundType
import com.example.data.model.AppThemeSetting
import com.example.data.model.DarkModeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserSettings(
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakInterval: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartPomodoro: Boolean = false,
    val appTheme: AppThemeSetting = AppThemeSetting.ZEN_FOREST,
    val darkMode: DarkModeSetting = DarkModeSetting.DARK,
    val eyeComfortTint: Boolean = false,
    val appBlockerEnabled: Boolean = true,
    val strictBlockerMode: Boolean = false,
    val ambientSound: AmbientSoundType = AmbientSoundType.ZEN_BELL,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val cloudSyncKey: String = "",
    val lastSyncTimeFormatted: String = "Never",
    val customAiKey: String = "",
    val dailyTargetMinutes: Int = 100
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aurafocus_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UserSettings {
        var syncKey = prefs.getString("cloud_sync_key", null)
        if (syncKey.isNullOrEmpty()) {
            syncKey = "AURA-" + UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString("cloud_sync_key", syncKey).apply()
        }

        return UserSettings(
            focusDurationMinutes = prefs.getInt("focus_duration", 25),
            shortBreakMinutes = prefs.getInt("short_break_duration", 5),
            longBreakMinutes = prefs.getInt("long_break_duration", 15),
            longBreakInterval = prefs.getInt("long_break_interval", 4),
            autoStartBreaks = prefs.getBoolean("auto_start_breaks", false),
            autoStartPomodoro = prefs.getBoolean("auto_start_pomodoro", false),
            appTheme = try {
                AppThemeSetting.valueOf(prefs.getString("app_theme", AppThemeSetting.ZEN_FOREST.name) ?: AppThemeSetting.ZEN_FOREST.name)
            } catch (e: Exception) { AppThemeSetting.ZEN_FOREST },
            darkMode = try {
                DarkModeSetting.valueOf(prefs.getString("dark_mode", DarkModeSetting.DARK.name) ?: DarkModeSetting.DARK.name)
            } catch (e: Exception) { DarkModeSetting.DARK },
            eyeComfortTint = prefs.getBoolean("eye_comfort_tint", false),
            appBlockerEnabled = prefs.getBoolean("app_blocker_enabled", true),
            strictBlockerMode = prefs.getBoolean("strict_blocker_mode", false),
            ambientSound = try {
                AmbientSoundType.valueOf(prefs.getString("ambient_sound", AmbientSoundType.ZEN_BELL.name) ?: AmbientSoundType.ZEN_BELL.name)
            } catch (e: Exception) { AmbientSoundType.ZEN_BELL },
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            cloudSyncKey = syncKey,
            lastSyncTimeFormatted = prefs.getString("last_sync_time", "Never") ?: "Never",
            customAiKey = prefs.getString("custom_ai_key", "") ?: "",
            dailyTargetMinutes = prefs.getInt("daily_target_minutes", 100)
        )
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit()
            .putInt("focus_duration", newSettings.focusDurationMinutes)
            .putInt("short_break_duration", newSettings.shortBreakMinutes)
            .putInt("long_break_duration", newSettings.longBreakMinutes)
            .putInt("long_break_interval", newSettings.longBreakInterval)
            .putBoolean("auto_start_breaks", newSettings.autoStartBreaks)
            .putBoolean("auto_start_pomodoro", newSettings.autoStartPomodoro)
            .putString("app_theme", newSettings.appTheme.name)
            .putString("dark_mode", newSettings.darkMode.name)
            .putBoolean("eye_comfort_tint", newSettings.eyeComfortTint)
            .putBoolean("app_blocker_enabled", newSettings.appBlockerEnabled)
            .putBoolean("strict_blocker_mode", newSettings.strictBlockerMode)
            .putString("ambient_sound", newSettings.ambientSound.name)
            .putBoolean("sound_enabled", newSettings.soundEnabled)
            .putBoolean("vibration_enabled", newSettings.vibrationEnabled)
            .putString("cloud_sync_key", newSettings.cloudSyncKey)
            .putString("last_sync_time", newSettings.lastSyncTimeFormatted)
            .putString("custom_ai_key", newSettings.customAiKey)
            .putInt("daily_target_minutes", newSettings.dailyTargetMinutes)
            .apply()
        _settingsFlow.value = newSettings
    }
}
