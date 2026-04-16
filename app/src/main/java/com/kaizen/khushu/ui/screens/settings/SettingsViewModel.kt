package com.kaizen.khushu.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.util.AppIconManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val appContext: Context,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettings(
            hapticsEnabled = true,
            dynamicColor = true,
            pureBlack = false,
            keepScreenAwake = true,
            volumeCounting = false,
            themeMode = "System",
            showStepTimer = true,
            fluidTransitions = true,
            vibrationOnCount = true,
            showLapCounter = true,
            showExitButton = true,
            showCompletionText = true,
            completionText = "الحمد لله",
            colorSeed = "default",
            tasbeehListMode = false,
            startupTab = "salah",
            tasbihBeadStyle = "CLASSIC_AMBER",
            showTajweed = false
        )
    )

    val isSettingsLoaded: StateFlow<Boolean> = repository.settingsFlow
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { repository.updateHaptics(enabled) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.updateDynamicColor(enabled) }
    }

    fun togglePureBlack(enabled: Boolean) {
        viewModelScope.launch { repository.updatePureBlack(enabled) }
    }

    fun toggleKeepScreenAwake(enabled: Boolean) {
        viewModelScope.launch { repository.updateKeepScreenAwake(enabled) }
    }

    fun toggleVolumeCounting(enabled: Boolean) {
        viewModelScope.launch { repository.updateVolumeCounting(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    fun toggleShowStepTimer(show: Boolean) {
        viewModelScope.launch { repository.updateShowStepTimer(show) }
    }

    fun toggleFluidTransitions(enabled: Boolean) {
        viewModelScope.launch { repository.updateFluidTransitions(enabled) }
    }

    fun toggleVibrationOnCount(enabled: Boolean) {
        viewModelScope.launch { repository.updateVibrationOnCount(enabled) }
    }

    fun toggleShowLapCounter(show: Boolean) {
        viewModelScope.launch { repository.updateShowLapCounter(show) }
    }

    fun updateShowExitButton(show: Boolean) {
        viewModelScope.launch { repository.updateShowExitButton(show) }
    }

    fun updateShowCompletionText(show: Boolean) {
        viewModelScope.launch { repository.updateShowCompletionText(show) }
    }

    fun updateCompletionText(text: String) {
        viewModelScope.launch { repository.updateCompletionText(text) }
    }

    fun setColorSeed(seed: String) {
        viewModelScope.launch { repository.updateColorSeed(seed) }
    }

    fun toggleTasbeehListMode(isList: Boolean) {
        viewModelScope.launch { repository.updateTasbeehListMode(isList) }
    }

    fun setStartupTab(route: String) {
        viewModelScope.launch { repository.updateStartupTab(route) }
    }

    fun setTasbihBeadStyle(style: String) {
        viewModelScope.launch { repository.updateTasbihBeadStyle(style) }
    }

    fun setLogoStyle(style: String) {
        val current = settings.value.logoStyle
        viewModelScope.launch {
            repository.updateLogoStyle(style)        // DataStore → instant in-app UI update
            if (style != current) {
                Toast.makeText(
                    appContext,
                    "Updating app icon. Khushu will restart momentarily...",
                    Toast.LENGTH_SHORT,
                ).show()
                delay(1500L)
                AppIconManager.apply(appContext, style)
            }
        }
    }

    fun setReadingTheme(theme: String) {
        viewModelScope.launch { repository.updateReadingTheme(theme) }
    }

    fun setArabicSizeSp(size: Float) {
        viewModelScope.launch { repository.updateArabicSizeSp(size) }
    }

    fun setTranslationSizeSp(size: Float) {
        viewModelScope.launch { repository.updateTranslationSizeSp(size) }
    }

    fun toggleShowTranslation(show: Boolean) {
        viewModelScope.launch { repository.updateShowTranslation(show) }
    }

    fun toggleShowTransliteration(show: Boolean) {
        viewModelScope.launch { repository.updateShowTransliteration(show) }
    }

    fun toggleShowWordByWord(show: Boolean) {
        viewModelScope.launch { repository.updateShowWordByWord(show) }
    }

    fun toggleReadingKeepScreenOn(keep: Boolean) {
        viewModelScope.launch { repository.updateReadingKeepScreenOn(keep) }
    }

    fun toggleShowContinueReading(show: Boolean) {
        viewModelScope.launch { repository.updateShowContinueReading(show) }
    }

    fun toggleShowTajweed(show: Boolean) {
        viewModelScope.launch { repository.updateShowTajweed(show) }
    }

    fun updateLastReadTopicId(id: String) {
        viewModelScope.launch { repository.updateLastReadTopicId(id) }
    }

    fun clearLastReadTopicId() {
        viewModelScope.launch { repository.updateLastReadTopicId("") }
    }

    fun toggleBookmark(topicId: String, ayahIndex: Int = 0) {
        viewModelScope.launch {
            val key = "$topicId:$ayahIndex"
            val current = settings.value.bookmarkedAyahs
            val updated = if (current.contains(key)) current - key else current + key
            repository.updateBookmarkedTopicIds(updated)
        }
    }

    fun toggleMastered(topicId: String) {
        viewModelScope.launch {
            val current = settings.value.masteredTopicIds
            val updated = if (current.contains(topicId)) current - topicId else current + topicId
            repository.updateMasteredTopicIds(updated)
        }
    }

    companion object {
        fun factory(repository: SettingsRepository, appContext: Context) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(repository, appContext) as T
                }
            }
    }
}
