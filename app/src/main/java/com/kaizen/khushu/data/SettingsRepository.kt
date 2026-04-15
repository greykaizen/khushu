package com.kaizen.khushu.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val VOLUME_COUNTING = booleanPreferencesKey("volume_counting")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHOW_STEP_TIMER = booleanPreferencesKey("show_step_timer")
        val FLUID_TRANSITIONS = booleanPreferencesKey("fluid_transitions")
        val VIBRATION_ON_COUNT = booleanPreferencesKey("vibration_on_count")
        val SHOW_LAP_COUNTER = booleanPreferencesKey("show_lap_counter")
        val SHOW_EXIT_BUTTON = booleanPreferencesKey("show_exit_button")
        val SHOW_COMPLETION_TEXT = booleanPreferencesKey("show_completion_text")
        val COMPLETION_TEXT = stringPreferencesKey("completion_text")
        val COLOR_SEED = stringPreferencesKey("color_seed")
        val TASBEEH_LIST_MODE = booleanPreferencesKey("tasbeeh_list_mode")
        val STARTUP_TAB = stringPreferencesKey("startup_tab")
        val TASBIH_BEAD_STYLE = stringPreferencesKey("tasbih_bead_style")
        val LOGO_STYLE = stringPreferencesKey("logo_style")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            UserSettings(
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                pureBlack = preferences[PreferencesKeys.PURE_BLACK] ?: false,
                keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: true,
                volumeCounting = preferences[PreferencesKeys.VOLUME_COUNTING] ?: false,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "System",
                showStepTimer = preferences[PreferencesKeys.SHOW_STEP_TIMER] ?: true,
                fluidTransitions = preferences[PreferencesKeys.FLUID_TRANSITIONS] ?: true,
                vibrationOnCount = preferences[PreferencesKeys.VIBRATION_ON_COUNT] ?: true,
                showLapCounter = preferences[PreferencesKeys.SHOW_LAP_COUNTER] ?: true,
                showExitButton = preferences[PreferencesKeys.SHOW_EXIT_BUTTON] ?: true,
                showCompletionText = preferences[PreferencesKeys.SHOW_COMPLETION_TEXT] ?: true,
                completionText = preferences[PreferencesKeys.COMPLETION_TEXT] ?: "الحمد لله",
                colorSeed = preferences[PreferencesKeys.COLOR_SEED] ?: "default",
                tasbeehListMode = preferences[PreferencesKeys.TASBEEH_LIST_MODE] ?: false,
                startupTab = preferences[PreferencesKeys.STARTUP_TAB] ?: "salah",
                tasbihBeadStyle = preferences[PreferencesKeys.TASBIH_BEAD_STYLE] ?: "CLASSIC_AMBER",
                logoStyle = preferences[PreferencesKeys.LOGO_STYLE] ?: "DYNAMIC",
            )
        }

    suspend fun updateHaptics(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updatePureBlack(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PURE_BLACK] = enabled }
    }

    suspend fun updateKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun updateVolumeCounting(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VOLUME_COUNTING] = enabled }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun updateShowStepTimer(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_STEP_TIMER] = show }
    }

    suspend fun updateFluidTransitions(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FLUID_TRANSITIONS] = enabled }
    }

    suspend fun updateVibrationOnCount(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ON_COUNT] = enabled }
    }

    suspend fun updateShowLapCounter(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_LAP_COUNTER] = show }
    }

    suspend fun updateShowExitButton(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_EXIT_BUTTON] = show }
    }

    suspend fun updateShowCompletionText(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_COMPLETION_TEXT] = show }
    }

    suspend fun updateCompletionText(text: String) {
        context.dataStore.edit { it[PreferencesKeys.COMPLETION_TEXT] = text }
    }

    suspend fun updateColorSeed(seed: String) {
        context.dataStore.edit { it[PreferencesKeys.COLOR_SEED] = seed }
    }

    suspend fun updateTasbeehListMode(isList: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASBEEH_LIST_MODE] = isList }
    }

    suspend fun updateStartupTab(route: String) {
        context.dataStore.edit { it[PreferencesKeys.STARTUP_TAB] = route }
    }

    suspend fun updateTasbihBeadStyle(style: String) {
        context.dataStore.edit { it[PreferencesKeys.TASBIH_BEAD_STYLE] = style }
    }

    suspend fun updateLogoStyle(style: String) {
        context.dataStore.edit { it[PreferencesKeys.LOGO_STYLE] = style }
    }
}

data class UserSettings(
    val hapticsEnabled: Boolean,
    val dynamicColor: Boolean,
    val pureBlack: Boolean,
    val keepScreenAwake: Boolean,
    val volumeCounting: Boolean,
    val themeMode: String,
    val showStepTimer: Boolean,
    val fluidTransitions: Boolean,
    val vibrationOnCount: Boolean,
    val showLapCounter: Boolean,
    val showExitButton: Boolean,
    val showCompletionText: Boolean,
    val completionText: String,
    val colorSeed: String,
    val tasbeehListMode: Boolean,
    val startupTab: String,
    val tasbihBeadStyle: String,
    val logoStyle: String = "DYNAMIC",
)
