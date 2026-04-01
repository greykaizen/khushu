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
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "System"
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
}

data class UserSettings(
    val hapticsEnabled: Boolean,
    val dynamicColor: Boolean,
    val pureBlack: Boolean,
    val keepScreenAwake: Boolean,
    val volumeCounting: Boolean,
    val themeMode: String
)
