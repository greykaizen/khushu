package com.kaizen.khushu.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.SettingsRepository
import com.kaizen.khushu.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

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
            tasbihBeadStyle = "CLASSIC_AMBER"
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

    companion object {
        fun factory(repository: SettingsRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
        }
    }
}
