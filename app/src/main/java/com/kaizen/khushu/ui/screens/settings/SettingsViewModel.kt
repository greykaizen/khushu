package com.kaizen.khushu.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.SettingsRepository
import com.kaizen.khushu.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings(
            hapticsEnabled = true,
            dynamicColor = true,
            pureBlack = false,
            keepScreenAwake = true,
            volumeCounting = false,
            themeMode = "System"
        )
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

    companion object {
        fun factory(repository: SettingsRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
        }
    }
}
