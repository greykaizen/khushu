package com.kaizen.khushu.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.kaizen.khushu.data.repository.EXTRA_PRAYER_TIMINGS
import com.kaizen.khushu.receiver.PrayerAlarmReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val calculationMethodLabels = mapOf(
    "MUSLIM_WORLD_LEAGUE" to "Muslim World League",
    "EGYPTIAN" to "Egyptian General Authority of Survey",
    "KARACHI" to "University of Islamic Sciences, Karachi",
    "UMM_AL_QURA" to "Umm al-Qura University, Makkah",
    "DUBAI" to "Dubai, UAE",
    "MOON_SIGHTING_COMMITTEE" to "Moonsighting Committee Worldwide",
    "NORTH_AMERICA" to "Islamic Society of North America",
    "KUWAIT" to "Kuwait",
    "QATAR" to "Qatar",
    "SINGAPORE" to "Majlis Ugama Islam Singapura",
    "TEHRAN" to "Institute of Geophysics, University of Tehran",
    "TURKEY" to "Diyanet Isleri Baskanligi, Turkey"
)

private val madhabLabels = mapOf(
    "SHAFI" to "Standard (Shafi, Maliki, Hanbali)",
    "HANAFI" to "Hanafi"
)

private val sourceLabels = mapOf(
    "LOCAL" to "Local Formula (Offline)",
    "API" to "AlAdhan API (Online)"
)

private val alertStyleLabels = mapOf(
    "CUSTOM_SOUND" to "Custom sound",
    "SYSTEM_SOUND" to "System sound",
    "VIBRATION" to "Vibration only",
    "SILENT" to "Silent"
)

private fun prayerSettingLabel(value: String, labels: Map<String, String>): String {
    return labels[value] ?: value
}

private data class PrayerNotificationPreference(
    val prayerName: String,
    val prayerEnabled: Boolean,
    val prePrayerEnabled: Boolean,
    val prePrayerMinutes: Int,
)

private data class ExtraTimingPreference(
    val id: String,
    val label: String,
    val selected: Boolean,
    val notificationEnabled: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val lastRefreshed = if (settings.lastPrayerRefreshEpochMs > 0L) {
        SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(settings.lastPrayerRefreshEpochMs))
    } else {
        "Not refreshed yet"
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.refreshLocation()
        }
    }
    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val calculationMethods = listOf(
        "MUSLIM_WORLD_LEAGUE", "EGYPTIAN", "KARACHI", "UMM_AL_QURA",
        "DUBAI", "MOON_SIGHTING_COMMITTEE", "NORTH_AMERICA", "KUWAIT",
        "QATAR", "SINGAPORE", "TEHRAN", "TURKEY"
    )
    val madhabs = listOf("SHAFI", "HANAFI")
    val sources = listOf("LOCAL", "API")
    val alertStyleOptions = listOf("CUSTOM_SOUND", "SYSTEM_SOUND", "VIBRATION", "SILENT")
    val prayerNotificationPreferences = listOf(
        PrayerNotificationPreference("Fajr", settings.fajrPrayerNotificationEnabled, settings.fajrPrePrayerNotificationEnabled, settings.fajrPrePrayerMinutes),
        PrayerNotificationPreference("Dhuhr", settings.dhuhrPrayerNotificationEnabled, settings.dhuhrPrePrayerNotificationEnabled, settings.dhuhrPrePrayerMinutes),
        PrayerNotificationPreference("Asr", settings.asrPrayerNotificationEnabled, settings.asrPrePrayerNotificationEnabled, settings.asrPrePrayerMinutes),
        PrayerNotificationPreference("Maghrib", settings.maghribPrayerNotificationEnabled, settings.maghribPrePrayerNotificationEnabled, settings.maghribPrePrayerMinutes),
        PrayerNotificationPreference("Isha", settings.ishaPrayerNotificationEnabled, settings.ishaPrePrayerNotificationEnabled, settings.ishaPrePrayerMinutes)
    )
    val extraTimingPreferences = EXTRA_PRAYER_TIMINGS.map {
        ExtraTimingPreference(
            id = it.id,
            label = it.label,
            selected = settings.selectedExtraPrayerTimings.contains(it.id),
            notificationEnabled = settings.extraPrayerNotifications.contains(it.id)
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Prayer Times", scrollBehavior) },
                navigationIcon = { SettingsBackButton(onBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsSectionCard(
                title = "Calculation",
                subtitle = "How Khushu computes the daily prayer times."
            ) {
                SettingsDropdown(
                    title = "Convention",
                    subtitle = "The calculation convention used for Fajr and Isha.",
                    options = calculationMethods,
                    selectedOption = settings.prayerCalculationMethod,
                    optionLabel = { prayerSettingLabel(it, calculationMethodLabels) },
                    onOptionSelected = viewModel::setPrayerCalculationMethod
                )

                SettingsDropdown(
                    title = "Madhab",
                    subtitle = "The Asr shadow rule used in the calculation.",
                    options = madhabs,
                    selectedOption = settings.prayerMadhab,
                    optionLabel = { prayerSettingLabel(it, madhabLabels) },
                    onOptionSelected = viewModel::setPrayerMadhab
                )

                SettingsDropdown(
                    title = "Source",
                    subtitle = "Choose the local formula or the online AlAdhan source.",
                    options = sources,
                    selectedOption = settings.prayerSourceType,
                    optionLabel = { prayerSettingLabel(it, sourceLabels) },
                    onOptionSelected = viewModel::setPrayerSourceType
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionCard(
                title = "Location",
                subtitle = "Use GPS or your saved coordinates for local prayer times."
            ) {
                SettingsToggle(
                    title = "Use Device GPS",
                    subtitle = "Automatically refresh your location for accurate local timings.",
                    checked = settings.useGpsLocation,
                    onCheckedChange = { enabled ->
                        viewModel.toggleUseGpsLocation(enabled)
                        if (enabled) {
                            viewModel.refreshLocation()
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("GPS Access")
                    }

                    OutlinedButton(
                        onClick = viewModel::refreshLocation,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Refresh")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text(
                    text = "Current: ${"%.4f".format(Locale.US, settings.locationLat)}, ${"%.4f".format(Locale.US, settings.locationLng)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last refreshed: $lastRefreshed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionCard(
                title = "Reminders",
                subtitle = "Global alert style and per-prayer reminder controls."
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
                    FilledTonalButton(
                        onClick = {
                            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Allow Notifications")
                    }
                }

                SettingsDropdown(
                    title = "Alert style",
                    subtitle = "Choose how prayer reminders sound or behave.",
                    options = alertStyleOptions,
                    selectedOption = settings.prayerNotificationAlertStyle,
                    optionLabel = { alertStyleLabels[it] ?: it },
                    onOptionSelected = viewModel::setPrayerNotificationAlertStyle
                )

                prayerNotificationPreferences.forEach { preference ->
                    PrayerReminderCard(
                        preference = preference,
                        onPrayerToggle = { enabled -> viewModel.setPrayerNotificationEnabled(preference.prayerName, enabled) },
                        onPrePrayerToggle = { enabled -> viewModel.setPrePrayerNotificationEnabled(preference.prayerName, enabled) },
                        onPrePrayerMinutesChange = { minutes ->
                            viewModel.setPrePrayerMinutes(preference.prayerName, minutes)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionCard(
                title = "Extra Timings",
                subtitle = "Optional non-fard timings and whether they appear on Home."
            ) {
                extraTimingPreferences.forEach { preference ->
                    ExtraTimingCard(
                        preference = preference,
                        onSelectedToggle = { enabled -> viewModel.toggleExtraPrayerTiming(preference.id, enabled) },
                        onNotificationToggle = { enabled -> viewModel.toggleExtraPrayerNotification(preference.id, enabled) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                SettingsToggle(
                    title = "Show extra timings on Home",
                    subtitle = "Include selected extra timings on the next card, sun card, and prayer slab.",
                    checked = settings.showExtraPrayerTimingsOnHome,
                    onCheckedChange = viewModel::toggleShowExtraPrayerTimingsOnHome
                )
                SettingsToggle(
                    title = "Show Islamic events on Home",
                    subtitle = "Keep the monthly Islamic events strip visible on the Home screen.",
                    checked = settings.showUpcomingEventsOnHome,
                    onCheckedChange = viewModel::toggleShowUpcomingEventsOnHome
                )
            }

            Spacer(Modifier.height(16.dp))

            ExpandableSettingsCard(
                title = "Advanced",
                summary = "Offsets, diagnostics, and debugging tools."
            ) {
                Text("Prayer Offsets", style = MaterialTheme.typography.titleSmall)
                PrayerOffsetRow("Fajr", settings.fajrOffsetMinutes) { viewModel.setPrayerOffset("Fajr", it) }
                PrayerOffsetRow("Dhuhr", settings.dhuhrOffsetMinutes) { viewModel.setPrayerOffset("Dhuhr", it) }
                PrayerOffsetRow("Asr", settings.asrOffsetMinutes) { viewModel.setPrayerOffset("Asr", it) }
                PrayerOffsetRow("Maghrib", settings.maghribOffsetMinutes) { viewModel.setPrayerOffset("Maghrib", it) }
                PrayerOffsetRow("Isha", settings.ishaOffsetMinutes) { viewModel.setPrayerOffset("Isha", it) }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text("Diagnostics", style = MaterialTheme.typography.titleSmall)
                PrayerDiagnosticsCard(
                    source = prayerSettingLabel(settings.prayerSourceType, sourceLabels),
                    method = prayerSettingLabel(settings.prayerCalculationMethod, calculationMethodLabels),
                    madhab = prayerSettingLabel(settings.prayerMadhab, madhabLabels),
                    latitude = settings.locationLat,
                    longitude = settings.locationLng,
                    lastRefreshed = lastRefreshed,
                    gpsEnabled = settings.useGpsLocation,
                    notificationsAllowed = notificationsAllowed
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text("Debug", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = {
                            context.sendBroadcast(
                                Intent(context, PrayerAlarmReceiver::class.java).apply {
                                    action = PrayerAlarmReceiver.ACTION_FIRE_PRAYER_NOTIFICATION
                                    putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, "Dhuhr")
                                    putExtra(PrayerAlarmReceiver.EXTRA_NOTIFICATION_TYPE, "PRAYER")
                                    putExtra(PrayerAlarmReceiver.EXTRA_PRE_PRAYER_MINUTES, 0)
                                    putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT_MILLIS, System.currentTimeMillis())
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Debug Prayer")
                    }
                    OutlinedButton(
                        onClick = {
                            context.sendBroadcast(
                                Intent(context, PrayerAlarmReceiver::class.java).apply {
                                    action = PrayerAlarmReceiver.ACTION_FIRE_PRAYER_NOTIFICATION
                                    putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, "Dhuhr")
                                    putExtra(PrayerAlarmReceiver.EXTRA_NOTIFICATION_TYPE, "PRE_PRAYER")
                                    putExtra(PrayerAlarmReceiver.EXTRA_PRE_PRAYER_MINUTES, 10)
                                    putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT_MILLIS, System.currentTimeMillis())
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Debug Pre")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrayerOffsetRow(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Adjust by minutes after calculation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        OutlinedButton(onClick = { onValueChange(value - 1) }) {
            Text("-")
        }
        Text(
            text = if (value > 0) "+$value min" else "$value min",
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(onClick = { onValueChange(value + 1) }) {
            Text("+")
        }
    }
}

@Composable
private fun ExtraTimingCard(
    preference: ExtraTimingPreference,
    onSelectedToggle: (Boolean) -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
) {
    val summary = buildString {
        append(if (preference.selected) "Shown" else "Hidden")
        append(" · ")
        append(if (preference.notificationEnabled) "Alert on" else "Alert off")
    }
    ExpandableSettingsCard(
        title = preference.label,
        summary = summary,
        actions = {
            SettingsInlineAction(
                selected = preference.selected,
                icon = Icons.Default.Schedule,
                contentDescription = "Show ${preference.label}",
                onClick = { onSelectedToggle(!preference.selected) }
            )
            SettingsInlineAction(
                selected = preference.notificationEnabled,
                icon = Icons.Default.Notifications,
                contentDescription = "Notify for ${preference.label}",
                onClick = { onNotificationToggle(!preference.notificationEnabled) }
            )
        }
    ) {
        SettingsToggle(
            title = "Show on Home and extra timing lists",
            subtitle = "Keep ${preference.label} available in Khushu surfaces.",
            checked = preference.selected,
            onCheckedChange = onSelectedToggle
        )
        SettingsToggle(
            title = "Alert me when it begins",
            subtitle = "Send a reminder when ${preference.label} starts.",
            checked = preference.notificationEnabled,
            onCheckedChange = onNotificationToggle
        )
    }
}

@Composable
private fun PrayerReminderCard(
    preference: PrayerNotificationPreference,
    onPrayerToggle: (Boolean) -> Unit,
    onPrePrayerToggle: (Boolean) -> Unit,
    onPrePrayerMinutesChange: (Int) -> Unit,
) {
    val summary = buildString {
        append(if (preference.prayerEnabled) "Prayer on" else "Prayer off")
        append(" · ")
        if (preference.prePrayerEnabled) append("Pre ${preference.prePrayerMinutes}m") else append("Pre off")
    }
    ExpandableSettingsCard(
        title = preference.prayerName,
        summary = summary,
        actions = {
            SettingsInlineAction(
                selected = preference.prayerEnabled,
                icon = Icons.Default.Notifications,
                contentDescription = "${preference.prayerName} prayer reminder",
                onClick = { onPrayerToggle(!preference.prayerEnabled) }
            )
            SettingsInlineAction(
                selected = preference.prePrayerEnabled,
                icon = Icons.Default.Schedule,
                contentDescription = "${preference.prayerName} pre-prayer reminder",
                onClick = { onPrePrayerToggle(!preference.prePrayerEnabled) }
            )
        }
    ) {
        SettingsToggle(
            title = "Prayer notification",
            subtitle = "Alert when ${preference.prayerName} starts.",
            checked = preference.prayerEnabled,
            onCheckedChange = onPrayerToggle
        )
        SettingsToggle(
            title = "Pre-prayer notification",
            subtitle = "Remind me before ${preference.prayerName}.",
            checked = preference.prePrayerEnabled,
            onCheckedChange = onPrePrayerToggle
        )
        if (preference.prePrayerEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lead time", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${preference.prePrayerMinutes} minutes before",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                OutlinedButton(onClick = { onPrePrayerMinutesChange(preference.prePrayerMinutes - 1) }) {
                    Text("-")
                }
                Text(
                    text = "${preference.prePrayerMinutes}m",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = { onPrePrayerMinutesChange(preference.prePrayerMinutes + 1) }) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
private fun PrayerDiagnosticsCard(
    source: String,
    method: String,
    madhab: String,
    latitude: Float,
    longitude: Float,
    lastRefreshed: String,
    gpsEnabled: Boolean,
    notificationsAllowed: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Current Prayer Timing Setup", style = MaterialTheme.typography.titleMedium)
            Text("Source: $source", style = MaterialTheme.typography.bodyMedium)
            Text("Method: $method", style = MaterialTheme.typography.bodyMedium)
            Text("Madhab: $madhab", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Coordinates: ${"%.4f".format(Locale.US, latitude)}, ${"%.4f".format(Locale.US, longitude)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("GPS: ${if (gpsEnabled) "Enabled" else "Disabled"}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Notifications: ${if (notificationsAllowed) "Allowed" else "Not allowed"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Delivery: Battery-friendly scheduled notification", style = MaterialTheme.typography.bodyMedium)
            Text("Last Refresh: $lastRefreshed", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
