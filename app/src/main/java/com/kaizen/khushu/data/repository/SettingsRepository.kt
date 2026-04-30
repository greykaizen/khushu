package com.kaizen.khushu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.kaizen.khushu.data.model.CustomBeadStyle
import com.kaizen.khushu.data.model.DEFAULT_CUSTOM_BEAD_STYLE_ID
import com.kaizen.khushu.data.model.defaultCustomBeadStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val READING_THEME = stringPreferencesKey("reading_theme")
        val ARABIC_SIZE_SP = floatPreferencesKey("arabic_size_sp")
        val TRANSLATION_SIZE_SP = floatPreferencesKey("translation_size_sp")
        val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val SHOW_WORD_BY_WORD = booleanPreferencesKey("show_word_by_word")
        val READING_KEEP_SCREEN_ON = booleanPreferencesKey("reading_keep_screen_on")
        val LAST_READ_TOPIC_ID = stringPreferencesKey("last_read_topic_id")
        val BOOKMARKED_TOPIC_IDS = stringSetPreferencesKey("bookmarked_topic_ids")
        val MASTERED_TOPIC_IDS = stringSetPreferencesKey("mastered_topic_ids")
        val SHOW_CONTINUE_READING = booleanPreferencesKey("show_continue_reading")
        val SHOW_TAJWEED = booleanPreferencesKey("show_tajweed")
        val SELECTED_TRANSLATION_LANG = stringPreferencesKey("selected_translation_lang")
        val SELECTED_TRANSLATION_SOURCE = stringPreferencesKey("selected_translation_source")
        val SELECTED_TAFSIR_ID = stringPreferencesKey("selected_tafsir_id")
        val SELECTED_TAFSIR_SOURCE = stringPreferencesKey("selected_tafsir_source")
        val SHOW_TAFSIR = booleanPreferencesKey("show_tafsir")
        val SELECTED_AUDIO_SOURCE = stringPreferencesKey("selected_audio_source")
        val SELECTED_RECITER_ID = stringPreferencesKey("selected_reciter_id")
        val SELECTED_SCRIPT = stringPreferencesKey("selected_script")
        val TASBEEH_DYNAMIC_COLORS = booleanPreferencesKey("tasbeeh_dynamic_colors")

        // Tasbih Physics
        val STRING_ELASTICITY = floatPreferencesKey("string_elasticity")
        val WOBBLE_STIFFNESS = floatPreferencesKey("wobble_stiffness")
        val WOBBLE_DAMPING_RATIO = floatPreferencesKey("wobble_damping_ratio")
        val BEAD_MICRO_SCALE = floatPreferencesKey("bead_micro_scale")

        // Custom Bead Styles
        val CUSTOM_BEAD_STYLES_JSON = stringPreferencesKey("custom_bead_styles_json")
        val ACTIVE_BEAD_STYLE_ID = stringPreferencesKey("active_bead_style_id")

        // Tasbeeh Interaction
        val TASBEEH_STEALTH_MODE_ALLOWED = booleanPreferencesKey("tasbeeh_stealth_mode_allowed")
        val TASBEEH_VOLUME_ENABLED = booleanPreferencesKey("tasbeeh_volume_enabled")
        val TASBEEH_VOLUME_ANIMATION = booleanPreferencesKey("tasbeeh_volume_animation")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Prayer Times & Location
        val PRAYER_CALCULATION_METHOD = stringPreferencesKey("prayer_calculation_method")
        val PRAYER_MADHAB = stringPreferencesKey("prayer_madhab")
        val LOCATION_LAT = floatPreferencesKey("location_lat")
        val LOCATION_LNG = floatPreferencesKey("location_lng")
        val USE_GPS_LOCATION = booleanPreferencesKey("use_gps_location")
        val PRAYER_SOURCE_TYPE = stringPreferencesKey("prayer_source_type")
        val PRAYER_OFFSET_FAJR = intPreferencesKey("prayer_offset_fajr")
        val PRAYER_OFFSET_DHUHR = intPreferencesKey("prayer_offset_dhuhr")
        val PRAYER_OFFSET_ASR = intPreferencesKey("prayer_offset_asr")
        val PRAYER_OFFSET_MAGHRIB = intPreferencesKey("prayer_offset_maghrib")
        val PRAYER_OFFSET_ISHA = intPreferencesKey("prayer_offset_isha")
        val LAST_PRAYER_REFRESH_EPOCH_MS = longPreferencesKey("last_prayer_refresh_epoch_ms")
        val PRAYER_NOTIFICATION_FAJR = booleanPreferencesKey("prayer_notification_fajr")
        val PRAYER_NOTIFICATION_DHUHR = booleanPreferencesKey("prayer_notification_dhuhr")
        val PRAYER_NOTIFICATION_ASR = booleanPreferencesKey("prayer_notification_asr")
        val PRAYER_NOTIFICATION_MAGHRIB = booleanPreferencesKey("prayer_notification_maghrib")
        val PRAYER_NOTIFICATION_ISHA = booleanPreferencesKey("prayer_notification_isha")
        val PRE_PRAYER_NOTIFICATION_FAJR = booleanPreferencesKey("pre_prayer_notification_fajr")
        val PRE_PRAYER_NOTIFICATION_DHUHR = booleanPreferencesKey("pre_prayer_notification_dhuhr")
        val PRE_PRAYER_NOTIFICATION_ASR = booleanPreferencesKey("pre_prayer_notification_asr")
        val PRE_PRAYER_NOTIFICATION_MAGHRIB = booleanPreferencesKey("pre_prayer_notification_maghrib")
        val PRE_PRAYER_NOTIFICATION_ISHA = booleanPreferencesKey("pre_prayer_notification_isha")
        val PRE_PRAYER_MINUTES_FAJR = intPreferencesKey("pre_prayer_minutes_fajr")
        val PRE_PRAYER_MINUTES_DHUHR = intPreferencesKey("pre_prayer_minutes_dhuhr")
        val PRE_PRAYER_MINUTES_ASR = intPreferencesKey("pre_prayer_minutes_asr")
        val PRE_PRAYER_MINUTES_MAGHRIB = intPreferencesKey("pre_prayer_minutes_maghrib")
        val PRE_PRAYER_MINUTES_ISHA = intPreferencesKey("pre_prayer_minutes_isha")
        val PRAYER_NOTIFICATION_ALERT_STYLE = stringPreferencesKey("prayer_notification_alert_style")
        val LAST_DELIVERED_PRAYER_NOTIFICATION_EVENT_ID = stringPreferencesKey("last_delivered_prayer_notification_event_id")
        val SELECTED_EXTRA_PRAYER_TIMINGS = stringSetPreferencesKey("selected_extra_prayer_timings")
        val EXTRA_PRAYER_NOTIFICATIONS = stringSetPreferencesKey("extra_prayer_notifications")
        val SHOW_EXTRA_PRAYER_TIMINGS_ON_HOME = booleanPreferencesKey("show_extra_prayer_timings_on_home")
        val SHOW_UPCOMING_EVENTS_ON_HOME = booleanPreferencesKey("show_upcoming_events_on_home")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val customStylesJson = preferences[PreferencesKeys.CUSTOM_BEAD_STYLES_JSON] ?: "[]"
            val customStyles = try {
                Json.decodeFromString<List<CustomBeadStyle>>(customStylesJson)
            } catch (e: Exception) {
                emptyList()
            }
            val resolvedCustomStyles = if (customStyles.isEmpty()) {
                listOf(defaultCustomBeadStyle())
            } else {
                customStyles
            }
            val requestedActiveStyleId = preferences[PreferencesKeys.ACTIVE_BEAD_STYLE_ID]
            val resolvedActiveStyleId = requestedActiveStyleId
                ?.takeIf { id -> resolvedCustomStyles.any { it.id == id } }
                ?: DEFAULT_CUSTOM_BEAD_STYLE_ID

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
                tasbeehListMode = preferences[PreferencesKeys.TASBEEH_LIST_MODE] ?: true,
                startupTab = preferences[PreferencesKeys.STARTUP_TAB] ?: "home",
                tasbihBeadStyle = preferences[PreferencesKeys.TASBIH_BEAD_STYLE] ?: "CLASSIC_AMBER",
                logoStyle = preferences[PreferencesKeys.LOGO_STYLE] ?: "DYNAMIC",
                readingTheme = preferences[PreferencesKeys.READING_THEME] ?: "AUTO",
                arabicSizeSp = preferences[PreferencesKeys.ARABIC_SIZE_SP] ?: 32f,
                translationSizeSp = preferences[PreferencesKeys.TRANSLATION_SIZE_SP] ?: 16f,
                showTranslation = preferences[PreferencesKeys.SHOW_TRANSLATION] ?: true,
                showTransliteration = preferences[PreferencesKeys.SHOW_TRANSLITERATION] ?: false,
                showWordByWord = preferences[PreferencesKeys.SHOW_WORD_BY_WORD] ?: true,
                readingKeepScreenOn = preferences[PreferencesKeys.READING_KEEP_SCREEN_ON] ?: true,
                lastReadTopicId = preferences[PreferencesKeys.LAST_READ_TOPIC_ID],
                bookmarkedAyahs = preferences[PreferencesKeys.BOOKMARKED_TOPIC_IDS] ?: emptySet(),
                masteredTopicIds = preferences[PreferencesKeys.MASTERED_TOPIC_IDS] ?: emptySet(),
                showContinueReading = preferences[PreferencesKeys.SHOW_CONTINUE_READING] ?: true,
                showTajweed = preferences[PreferencesKeys.SHOW_TAJWEED] ?: false,
                selectedTranslationLang = preferences[PreferencesKeys.SELECTED_TRANSLATION_LANG] ?: "en_20",
                selectedTranslationSource = preferences[PreferencesKeys.SELECTED_TRANSLATION_SOURCE] ?: "FAWAZ",
                selectedTafsirId = preferences[PreferencesKeys.SELECTED_TAFSIR_ID] ?: "",
                selectedTafsirSource = preferences[PreferencesKeys.SELECTED_TAFSIR_SOURCE] ?: "SPA5K",
                showTafsir = preferences[PreferencesKeys.SHOW_TAFSIR] ?: false,
                selectedReciterId = preferences[PreferencesKeys.SELECTED_RECITER_ID] ?: "mishari",
                selectedAudioSource = preferences[PreferencesKeys.SELECTED_AUDIO_SOURCE] ?: "MP3QURAN",
                selectedScript = preferences[PreferencesKeys.SELECTED_SCRIPT] ?: "uthmani",
                tasbeehDynamicColors = preferences[PreferencesKeys.TASBEEH_DYNAMIC_COLORS] ?: true,
                stringElasticity = preferences[PreferencesKeys.STRING_ELASTICITY] ?: 1.8f,
                wobbleStiffness = preferences[PreferencesKeys.WOBBLE_STIFFNESS] ?: 140f,
                wobbleDampingRatio = preferences[PreferencesKeys.WOBBLE_DAMPING_RATIO] ?: 0.25f,
                beadMicroScale = preferences[PreferencesKeys.BEAD_MICRO_SCALE] ?: 1.2f,
                customBeadStyles = resolvedCustomStyles,
                activeBeadStyleId = resolvedActiveStyleId,
                tasbeehStealthModeAllowed = preferences[PreferencesKeys.TASBEEH_STEALTH_MODE_ALLOWED] ?: false,
                tasbeehVolumeEnabled = preferences[PreferencesKeys.TASBEEH_VOLUME_ENABLED] ?: true,
                tasbeehVolumeAnimation = preferences[PreferencesKeys.TASBEEH_VOLUME_ANIMATION] ?: true,
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                prayerCalculationMethod = preferences[PreferencesKeys.PRAYER_CALCULATION_METHOD] ?: "MUSLIM_WORLD_LEAGUE",
                prayerMadhab = preferences[PreferencesKeys.PRAYER_MADHAB] ?: "SHAFI",
                locationLat = preferences[PreferencesKeys.LOCATION_LAT] ?: 21.4225f,
                locationLng = preferences[PreferencesKeys.LOCATION_LNG] ?: 39.8262f,
                useGpsLocation = preferences[PreferencesKeys.USE_GPS_LOCATION] ?: false,
                prayerSourceType = preferences[PreferencesKeys.PRAYER_SOURCE_TYPE] ?: "LOCAL",
                fajrOffsetMinutes = preferences[PreferencesKeys.PRAYER_OFFSET_FAJR] ?: 0,
                dhuhrOffsetMinutes = preferences[PreferencesKeys.PRAYER_OFFSET_DHUHR] ?: 0,
                asrOffsetMinutes = preferences[PreferencesKeys.PRAYER_OFFSET_ASR] ?: 0,
                maghribOffsetMinutes = preferences[PreferencesKeys.PRAYER_OFFSET_MAGHRIB] ?: 0,
                ishaOffsetMinutes = preferences[PreferencesKeys.PRAYER_OFFSET_ISHA] ?: 0,
                lastPrayerRefreshEpochMs = preferences[PreferencesKeys.LAST_PRAYER_REFRESH_EPOCH_MS] ?: 0L,
                fajrPrayerNotificationEnabled = preferences[PreferencesKeys.PRAYER_NOTIFICATION_FAJR] ?: false,
                dhuhrPrayerNotificationEnabled = preferences[PreferencesKeys.PRAYER_NOTIFICATION_DHUHR] ?: false,
                asrPrayerNotificationEnabled = preferences[PreferencesKeys.PRAYER_NOTIFICATION_ASR] ?: false,
                maghribPrayerNotificationEnabled = preferences[PreferencesKeys.PRAYER_NOTIFICATION_MAGHRIB] ?: false,
                ishaPrayerNotificationEnabled = preferences[PreferencesKeys.PRAYER_NOTIFICATION_ISHA] ?: false,
                fajrPrePrayerNotificationEnabled = preferences[PreferencesKeys.PRE_PRAYER_NOTIFICATION_FAJR] ?: false,
                dhuhrPrePrayerNotificationEnabled = preferences[PreferencesKeys.PRE_PRAYER_NOTIFICATION_DHUHR] ?: false,
                asrPrePrayerNotificationEnabled = preferences[PreferencesKeys.PRE_PRAYER_NOTIFICATION_ASR] ?: false,
                maghribPrePrayerNotificationEnabled = preferences[PreferencesKeys.PRE_PRAYER_NOTIFICATION_MAGHRIB] ?: false,
                ishaPrePrayerNotificationEnabled = preferences[PreferencesKeys.PRE_PRAYER_NOTIFICATION_ISHA] ?: false,
                fajrPrePrayerMinutes = preferences[PreferencesKeys.PRE_PRAYER_MINUTES_FAJR] ?: 10,
                dhuhrPrePrayerMinutes = preferences[PreferencesKeys.PRE_PRAYER_MINUTES_DHUHR] ?: 10,
                asrPrePrayerMinutes = preferences[PreferencesKeys.PRE_PRAYER_MINUTES_ASR] ?: 10,
                maghribPrePrayerMinutes = preferences[PreferencesKeys.PRE_PRAYER_MINUTES_MAGHRIB] ?: 10,
                ishaPrePrayerMinutes = preferences[PreferencesKeys.PRE_PRAYER_MINUTES_ISHA] ?: 10,
                prayerNotificationAlertStyle = preferences[PreferencesKeys.PRAYER_NOTIFICATION_ALERT_STYLE] ?: "SYSTEM_SOUND",
                lastDeliveredPrayerNotificationEventId = preferences[PreferencesKeys.LAST_DELIVERED_PRAYER_NOTIFICATION_EVENT_ID].orEmpty(),
                selectedExtraPrayerTimings = preferences[PreferencesKeys.SELECTED_EXTRA_PRAYER_TIMINGS] ?: emptySet(),
                extraPrayerNotifications = preferences[PreferencesKeys.EXTRA_PRAYER_NOTIFICATIONS] ?: emptySet(),
                showExtraPrayerTimingsOnHome = preferences[PreferencesKeys.SHOW_EXTRA_PRAYER_TIMINGS_ON_HOME] ?: false,
                showUpcomingEventsOnHome = preferences[PreferencesKeys.SHOW_UPCOMING_EVENTS_ON_HOME] ?: true
            )
        }

    suspend fun updateTasbeehStealthModeAllowed(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASBEEH_STEALTH_MODE_ALLOWED] = enabled }
    }

    suspend fun updatePrayerCalculationMethod(method: String) {
        context.dataStore.edit { it[PreferencesKeys.PRAYER_CALCULATION_METHOD] = method }
    }

    suspend fun updatePrayerMadhab(madhab: String) {
        context.dataStore.edit { it[PreferencesKeys.PRAYER_MADHAB] = madhab }
    }

    suspend fun updateLocation(lat: Float, lng: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.LOCATION_LAT] = lat
            it[PreferencesKeys.LOCATION_LNG] = lng
        }
    }

    suspend fun updateUseGpsLocation(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.USE_GPS_LOCATION] = enabled }
    }

    suspend fun updatePrayerSourceType(source: String) {
        context.dataStore.edit { it[PreferencesKeys.PRAYER_SOURCE_TYPE] = source }
    }

    suspend fun updatePrayerOffset(prayerKey: String, minutes: Int) {
        context.dataStore.edit {
            when (prayerKey) {
                "Fajr" -> it[PreferencesKeys.PRAYER_OFFSET_FAJR] = minutes
                "Dhuhr" -> it[PreferencesKeys.PRAYER_OFFSET_DHUHR] = minutes
                "Asr" -> it[PreferencesKeys.PRAYER_OFFSET_ASR] = minutes
                "Maghrib" -> it[PreferencesKeys.PRAYER_OFFSET_MAGHRIB] = minutes
                "Isha" -> it[PreferencesKeys.PRAYER_OFFSET_ISHA] = minutes
            }
        }
    }

    suspend fun updateLastPrayerRefresh(epochMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_PRAYER_REFRESH_EPOCH_MS] = epochMs }
    }

    suspend fun updatePrayerNotificationEnabled(prayerKey: String, enabled: Boolean) {
        context.dataStore.edit {
            when (prayerKey) {
                "Fajr" -> it[PreferencesKeys.PRAYER_NOTIFICATION_FAJR] = enabled
                "Dhuhr" -> it[PreferencesKeys.PRAYER_NOTIFICATION_DHUHR] = enabled
                "Asr" -> it[PreferencesKeys.PRAYER_NOTIFICATION_ASR] = enabled
                "Maghrib" -> it[PreferencesKeys.PRAYER_NOTIFICATION_MAGHRIB] = enabled
                "Isha" -> it[PreferencesKeys.PRAYER_NOTIFICATION_ISHA] = enabled
            }
        }
    }

    suspend fun updatePrePrayerNotificationEnabled(prayerKey: String, enabled: Boolean) {
        context.dataStore.edit {
            when (prayerKey) {
                "Fajr" -> it[PreferencesKeys.PRE_PRAYER_NOTIFICATION_FAJR] = enabled
                "Dhuhr" -> it[PreferencesKeys.PRE_PRAYER_NOTIFICATION_DHUHR] = enabled
                "Asr" -> it[PreferencesKeys.PRE_PRAYER_NOTIFICATION_ASR] = enabled
                "Maghrib" -> it[PreferencesKeys.PRE_PRAYER_NOTIFICATION_MAGHRIB] = enabled
                "Isha" -> it[PreferencesKeys.PRE_PRAYER_NOTIFICATION_ISHA] = enabled
            }
        }
    }

    suspend fun updatePrePrayerMinutes(prayerKey: String, minutes: Int) {
        context.dataStore.edit {
            when (prayerKey) {
                "Fajr" -> it[PreferencesKeys.PRE_PRAYER_MINUTES_FAJR] = minutes
                "Dhuhr" -> it[PreferencesKeys.PRE_PRAYER_MINUTES_DHUHR] = minutes
                "Asr" -> it[PreferencesKeys.PRE_PRAYER_MINUTES_ASR] = minutes
                "Maghrib" -> it[PreferencesKeys.PRE_PRAYER_MINUTES_MAGHRIB] = minutes
                "Isha" -> it[PreferencesKeys.PRE_PRAYER_MINUTES_ISHA] = minutes
            }
        }
    }

    suspend fun updatePrayerNotificationAlertStyle(style: String) {
        context.dataStore.edit { it[PreferencesKeys.PRAYER_NOTIFICATION_ALERT_STYLE] = style }
    }

    suspend fun updateLastDeliveredPrayerNotificationEventId(eventId: String) {
        context.dataStore.edit { it[PreferencesKeys.LAST_DELIVERED_PRAYER_NOTIFICATION_EVENT_ID] = eventId }
    }

    suspend fun updateSelectedExtraPrayerTimings(ids: Set<String>) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_EXTRA_PRAYER_TIMINGS] = ids }
    }

    suspend fun updateExtraPrayerNotifications(ids: Set<String>) {
        context.dataStore.edit { it[PreferencesKeys.EXTRA_PRAYER_NOTIFICATIONS] = ids }
    }

    suspend fun updateShowExtraPrayerTimingsOnHome(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_EXTRA_PRAYER_TIMINGS_ON_HOME] = enabled }
    }

    suspend fun updateShowUpcomingEventsOnHome(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_UPCOMING_EVENTS_ON_HOME] = enabled }
    }

    suspend fun setTasbeehVolumeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASBEEH_VOLUME_ENABLED] = enabled }
    }

    suspend fun setTasbeehVolumeAnimation(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASBEEH_VOLUME_ANIMATION] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }


    suspend fun updateCustomBeadStyles(styles: List<CustomBeadStyle>) {
        val json = Json.encodeToString(styles)
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_BEAD_STYLES_JSON] = json }
    }

    suspend fun updateActiveBeadStyleId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.ACTIVE_BEAD_STYLE_ID] = id }
    }

    suspend fun updateTasbeehDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TASBEEH_DYNAMIC_COLORS] = enabled }
    }

    suspend fun updateStringElasticity(value: Float) {
        context.dataStore.edit { it[PreferencesKeys.STRING_ELASTICITY] = value }
    }

    suspend fun updateWobbleStiffness(value: Float) {
        context.dataStore.edit { it[PreferencesKeys.WOBBLE_STIFFNESS] = value }
    }

    suspend fun updateWobbleDampingRatio(value: Float) {
        context.dataStore.edit { it[PreferencesKeys.WOBBLE_DAMPING_RATIO] = value }
    }

    suspend fun updateBeadMicroScale(value: Float) {
        context.dataStore.edit { it[PreferencesKeys.BEAD_MICRO_SCALE] = value }
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

    suspend fun updateReadingTheme(theme: String) {
        context.dataStore.edit { it[PreferencesKeys.READING_THEME] = theme }
    }

    suspend fun updateArabicSizeSp(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.ARABIC_SIZE_SP] = size }
    }

    suspend fun updateTranslationSizeSp(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.TRANSLATION_SIZE_SP] = size }
    }

    suspend fun updateShowTranslation(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TRANSLATION] = show }
    }

    suspend fun updateShowTransliteration(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TRANSLITERATION] = show }
    }

    suspend fun updateShowWordByWord(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_WORD_BY_WORD] = show }
    }

    suspend fun updateReadingKeepScreenOn(keep: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.READING_KEEP_SCREEN_ON] = keep }
    }

    suspend fun updateLastReadTopicId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.LAST_READ_TOPIC_ID] = id }
    }

    suspend fun updateBookmarkedTopicIds(ids: Set<String>) {
        context.dataStore.edit { it[PreferencesKeys.BOOKMARKED_TOPIC_IDS] = ids }
    }

    suspend fun updateMasteredTopicIds(ids: Set<String>) {
        context.dataStore.edit { it[PreferencesKeys.MASTERED_TOPIC_IDS] = ids }
    }

    suspend fun updateShowContinueReading(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CONTINUE_READING] = show }
    }

    suspend fun updateShowTajweed(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TAJWEED] = show }
    }

    suspend fun updateSelectedTranslationLang(lang: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_TRANSLATION_LANG] = lang }
    }

    suspend fun setSelectedTranslationSource(source: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_TRANSLATION_SOURCE] = source }
    }

    suspend fun setSelectedTafsir(id: String, source: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SELECTED_TAFSIR_ID] = id
            it[PreferencesKeys.SELECTED_TAFSIR_SOURCE] = source
        }
    }

    suspend fun setSelectedAudioSource(source: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_AUDIO_SOURCE] = source }
    }

    suspend fun setShowTafsir(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TAFSIR] = show }
    }

    suspend fun updateSelectedReciterId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_RECITER_ID] = id }
    }

    suspend fun updateSelectedScript(script: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_SCRIPT] = script }
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
    val readingTheme: String = "DARK",
    val arabicSizeSp: Float = 32f,
    val translationSizeSp: Float = 16f,
    val showTranslation: Boolean = true,
    val showTransliteration: Boolean = false,
    val showWordByWord: Boolean = true,
    val readingKeepScreenOn: Boolean = true,
    val lastReadTopicId: String? = null,
    val bookmarkedAyahs: Set<String> = emptySet(),
    val masteredTopicIds: Set<String> = emptySet(),
    val showContinueReading: Boolean = true,
    val showTajweed: Boolean = false,
    val selectedTranslationLang: String = "en_20",
    val selectedTranslationSource: String = "FAWAZ",
    val selectedTafsirId: String = "",
    val selectedTafsirSource: String = "SPA5K",
    val showTafsir: Boolean = false,
    val selectedReciterId: String = "mishari",
    val selectedAudioSource: String = "MP3QURAN",
    val selectedScript: String = "uthmani",
    val tasbeehDynamicColors: Boolean = true,
    val stringElasticity: Float = 1.8f,
    val wobbleStiffness: Float = 140f,
    val wobbleDampingRatio: Float = 0.25f,
    val beadMicroScale: Float = 1.2f,
    val customBeadStyles: List<CustomBeadStyle> = listOf(defaultCustomBeadStyle()),
    val activeBeadStyleId: String = DEFAULT_CUSTOM_BEAD_STYLE_ID,
    val tasbeehStealthModeAllowed: Boolean = false,
    val tasbeehVolumeEnabled: Boolean = true,
    val tasbeehVolumeAnimation: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val prayerCalculationMethod: String = "MUSLIM_WORLD_LEAGUE",
    val prayerMadhab: String = "SHAFI",
    val locationLat: Float = 21.4225f,
    val locationLng: Float = 39.8262f,
    val useGpsLocation: Boolean = false,
    val prayerSourceType: String = "LOCAL",
    val fajrOffsetMinutes: Int = 0,
    val dhuhrOffsetMinutes: Int = 0,
    val asrOffsetMinutes: Int = 0,
    val maghribOffsetMinutes: Int = 0,
    val ishaOffsetMinutes: Int = 0,
    val lastPrayerRefreshEpochMs: Long = 0L,
    val fajrPrayerNotificationEnabled: Boolean = false,
    val dhuhrPrayerNotificationEnabled: Boolean = false,
    val asrPrayerNotificationEnabled: Boolean = false,
    val maghribPrayerNotificationEnabled: Boolean = false,
    val ishaPrayerNotificationEnabled: Boolean = false,
    val fajrPrePrayerNotificationEnabled: Boolean = false,
    val dhuhrPrePrayerNotificationEnabled: Boolean = false,
    val asrPrePrayerNotificationEnabled: Boolean = false,
    val maghribPrePrayerNotificationEnabled: Boolean = false,
    val ishaPrePrayerNotificationEnabled: Boolean = false,
    val fajrPrePrayerMinutes: Int = 10,
    val dhuhrPrePrayerMinutes: Int = 10,
    val asrPrePrayerMinutes: Int = 10,
    val maghribPrePrayerMinutes: Int = 10,
    val ishaPrePrayerMinutes: Int = 10,
    val prayerNotificationAlertStyle: String = "SYSTEM_SOUND",
    val lastDeliveredPrayerNotificationEventId: String = "",
    val selectedExtraPrayerTimings: Set<String> = emptySet(),
    val extraPrayerNotifications: Set<String> = emptySet(),
    val showExtraPrayerTimingsOnHome: Boolean = false,
    val showUpcomingEventsOnHome: Boolean = true
)
