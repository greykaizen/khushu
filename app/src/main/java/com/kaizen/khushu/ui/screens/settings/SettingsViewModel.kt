package com.kaizen.khushu.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.model.CustomBeadStyle
import com.kaizen.khushu.data.model.DEFAULT_CUSTOM_BEAD_STYLE_ID
import com.kaizen.khushu.data.model.defaultCustomBeadStyle
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.util.AppIconManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
    private var hasAttemptedGpsRefresh = false

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
            tasbeehListMode = true,
            startupTab = "salah",
            tasbihBeadStyle = "CLASSIC_AMBER",
            showTajweed = false,
            customBeadStyles = listOf(defaultCustomBeadStyle()),
            activeBeadStyleId = DEFAULT_CUSTOM_BEAD_STYLE_ID,
        )
    )

    val isSettingsLoaded: StateFlow<Boolean> = repository.settingsFlow
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    init {
        viewModelScope.launch {
            settings.collect { current ->
                if (current.useGpsLocation && !hasAttemptedGpsRefresh) {
                    hasAttemptedGpsRefresh = true
                    refreshLocation()
                }
                if (!current.useGpsLocation) {
                    hasAttemptedGpsRefresh = false
                }
            }
        }
    }

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

    fun toggleTasbeehDynamicColors(enabled: Boolean) {
        viewModelScope.launch { repository.updateTasbeehDynamicColors(enabled) }
    }

    fun toggleTasbeehStealthModeAllowed(enabled: Boolean) {
        viewModelScope.launch { repository.updateTasbeehStealthModeAllowed(enabled) }
    }

    fun toggleTasbeehVolumeEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setTasbeehVolumeEnabled(enabled) }
    }

    fun toggleTasbeehVolumeAnimation(enabled: Boolean) {
        viewModelScope.launch { repository.setTasbeehVolumeAnimation(enabled) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { repository.setOnboardingCompleted(completed) }
    }


    fun setStringElasticity(value: Float) {
        viewModelScope.launch { repository.updateStringElasticity(value) }
    }

    fun setWobbleStiffness(value: Float) {
        viewModelScope.launch { repository.updateWobbleStiffness(value) }
    }

    fun setWobbleDampingRatio(value: Float) {
        viewModelScope.launch { repository.updateWobbleDampingRatio(value) }
    }

    fun setBeadMicroScale(value: Float) {
        viewModelScope.launch { repository.updateBeadMicroScale(value) }
    }

    fun saveCustomBeadStyle(style: CustomBeadStyle) {
        val currentList = settings.value.customBeadStyles.toMutableList()
        val index = currentList.indexOfFirst { it.id == style.id }
        if (index != -1) {
            currentList[index] = style
        } else {
            currentList.add(style)
        }
        viewModelScope.launch { repository.updateCustomBeadStyles(currentList) }
    }

    fun deleteCustomBeadStyle(id: String) {
        val newList = settings.value.customBeadStyles.filter { it.id != id }
        viewModelScope.launch { repository.updateCustomBeadStyles(newList) }
    }

    fun setActiveBeadStyleId(id: String) {
        viewModelScope.launch { repository.updateActiveBeadStyleId(id) }
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

    fun setSelectedReciterId(id: String) {
        viewModelScope.launch { repository.updateSelectedReciterId(id) }
    }

    fun setSelectedScript(script: String) {
        viewModelScope.launch { repository.updateSelectedScript(script) }
    }

    fun setSelectedTranslationLang(lang: String) {
        viewModelScope.launch { repository.updateSelectedTranslationLang(lang) }
    }

    fun setSelectedTranslationSource(source: String) {
        viewModelScope.launch { repository.setSelectedTranslationSource(source) }
    }

    fun setSelectedTafsir(id: String, source: String) {
        viewModelScope.launch { repository.setSelectedTafsir(id, source) }
    }

    fun setShowTafsir(show: Boolean) {
        viewModelScope.launch { repository.setShowTafsir(show) }
    }

    fun setSelectedAudioSource(source: String) {
        viewModelScope.launch { repository.setSelectedAudioSource(source) }
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

    fun setPrayerCalculationMethod(method: String) {
        viewModelScope.launch { repository.updatePrayerCalculationMethod(method) }
    }

    fun setPrayerMadhab(madhab: String) {
        viewModelScope.launch { repository.updatePrayerMadhab(madhab) }
    }

    fun setLocation(lat: Float, lng: Float) {
        viewModelScope.launch { repository.updateLocation(lat, lng) }
    }

    fun toggleUseGpsLocation(enabled: Boolean) {
        viewModelScope.launch { repository.updateUseGpsLocation(enabled) }
    }

    fun setPrayerSourceType(source: String) {
        viewModelScope.launch { repository.updatePrayerSourceType(source) }
    }

    fun setPrayerOffset(prayerName: String, minutes: Int) {
        viewModelScope.launch { repository.updatePrayerOffset(prayerName, minutes.coerceIn(-15, 15)) }
    }

    fun setPrayerNotificationEnabled(prayerName: String, enabled: Boolean) {
        viewModelScope.launch { repository.updatePrayerNotificationEnabled(prayerName, enabled) }
    }

    fun setPrePrayerNotificationEnabled(prayerName: String, enabled: Boolean) {
        viewModelScope.launch { repository.updatePrePrayerNotificationEnabled(prayerName, enabled) }
    }

    fun setPrePrayerMinutes(prayerName: String, minutes: Int) {
        viewModelScope.launch { repository.updatePrePrayerMinutes(prayerName, minutes.coerceIn(1, 60)) }
    }

    fun setPrayerNotificationAlertStyle(style: String) {
        viewModelScope.launch { repository.updatePrayerNotificationAlertStyle(style) }
    }

    fun refreshLocation() {
        val hasFineLocation =
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation =
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        setLocation(location.latitude.toFloat(), location.longitude.toFloat())
                    } else {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token
                        ).addOnSuccessListener { currentLocation ->
                            currentLocation?.let {
                                setLocation(it.latitude.toFloat(), it.longitude.toFloat())
                            }
                        }
                    }
                }
        } catch (e: SecurityException) {
            // Should not happen if permission check passed
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
