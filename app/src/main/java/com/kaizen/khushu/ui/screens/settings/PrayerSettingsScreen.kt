package com.kaizen.khushu.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.kaizen.khushu.ui.theme.BeVietnamPro
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
    "TURKEY" to "Diyanet Isleri Baskanligi, Turkey",
    "ALGERIA" to "Algerian Ministry of Religious Affairs",
    "TUNISIA" to "Tunisian Ministry of Religious Affairs",
    "FRANCE_UOIF" to "Union des Organisations Islamiques de France (12°)",
    "FRANCE_15" to "France (15°)",
    "FRANCE_18" to "France / Grande Mosquée de Paris"
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
        "QATAR", "SINGAPORE", "ALGERIA", "TUNISIA", "FRANCE_UOIF", 
        "FRANCE_15", "FRANCE_18", "TEHRAN", "TURKEY"
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
            Spacer(Modifier.height(12.dp))

            SettingsGroup(
                title = "Calculation",
                description = "How Khushu computes the daily prayer times."
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SettingsDropdown(
                        title = "Convention",
                        subtitle = "The calculation convention used for Fajr and Isha.",
                        options = calculationMethods,
                        selectedOption = settings.prayerCalculationMethod,
                        optionLabel = { prayerSettingLabel(it, calculationMethodLabels) },
                        onOptionSelected = viewModel::setPrayerCalculationMethod
                    )
                }

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SettingsDropdown(
                        title = "Madhab",
                        subtitle = "The Asr shadow rule used in the calculation.",
                        options = madhabs,
                        selectedOption = settings.prayerMadhab,
                        optionLabel = { prayerSettingLabel(it, madhabLabels) },
                        onOptionSelected = viewModel::setPrayerMadhab
                    )
                }

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SettingsDropdown(
                        title = "Source",
                        subtitle = "Choose the local formula or the online source.",
                        options = sources,
                        selectedOption = settings.prayerSourceType,
                        optionLabel = { prayerSettingLabel(it, sourceLabels) },
                        onOptionSelected = viewModel::setPrayerSourceType
                    )
                }
            }

            SettingsGroup(
                title = "Location",
                description = "Use GPS or your saved coordinates for local prayer times."
            ) {
                SettingsToggleItem(
                    title = "Use Device GPS",
                    subtitle = "Automatically refresh your location for accurate timings.",
                    checked = settings.useGpsLocation,
                    onCheckedChange = { enabled ->
                        viewModel.toggleUseGpsLocation(enabled)
                        if (enabled) {
                            viewModel.refreshLocation()
                        }
                    }
                )

                if (settings.useGpsLocation) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                Text("GPS Access", fontFamily = BeVietnamPro, style = MaterialTheme.typography.titleSmall)
                            }
    
                            OutlinedButton(
                                onClick = viewModel::refreshLocation,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Refresh", fontFamily = BeVietnamPro, style = MaterialTheme.typography.titleSmall)
                            }
                        }
    
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Current: ${"%.4f".format(Locale.US, settings.locationLat)}, ${"%.4f".format(Locale.US, settings.locationLng)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = BeVietnamPro),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Last refreshed: $lastRefreshed",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = BeVietnamPro),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    ManualLocationInput(
                        lat = settings.locationLat,
                        lng = settings.locationLng,
                        onLocationSave = { lat, lng ->
                            viewModel.setLocation(lat, lng)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SettingsGroup(
                title = "Reminders",
                description = "Global alert style and per-prayer reminder controls."
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        FilledTonalButton(
                            onClick = {
                                notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Allow Notifications", fontFamily = BeVietnamPro)
                        }
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SettingsDropdown(
                        title = "Alert style",
                        subtitle = "Choose how prayer reminders sound or behave.",
                        options = alertStyleOptions,
                        selectedOption = settings.prayerNotificationAlertStyle,
                        optionLabel = { alertStyleLabels[it] ?: it },
                        onOptionSelected = viewModel::setPrayerNotificationAlertStyle
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            }

            SettingsGroup(
                title = "Extra Timings",
                description = "Optional non-fard timings and whether they appear on Home."
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    extraTimingPreferences.forEach { preference ->
                        ExtraTimingCard(
                            preference = preference,
                            onSelectedToggle = { enabled -> viewModel.toggleExtraPrayerTiming(preference.id, enabled) },
                            onNotificationToggle = { enabled -> viewModel.toggleExtraPrayerNotification(preference.id, enabled) }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                SettingsToggleItem(
                    title = "Show extra timings on Home",
                    subtitle = "Include selected extra timings on Home surfaces.",
                    checked = settings.showExtraPrayerTimingsOnHome,
                    onCheckedChange = viewModel::toggleShowExtraPrayerTimingsOnHome
                )
                SettingsToggleItem(
                    title = "Show Islamic events on Home",
                    subtitle = "Keep the monthly events strip visible.",
                    checked = settings.showUpcomingEventsOnHome,
                    onCheckedChange = viewModel::toggleShowUpcomingEventsOnHome,
                    showDivider = false
                )
            }

            SettingsGroup(
                title = "Advanced",
                description = "Offsets, diagnostics, and debugging tools."
            ) {
                ExpandableSettingsCard(
                    title = "Prayer Offsets",
                    summary = "Manually shift calculated timings.",
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrayerOffsetRow("Fajr", settings.fajrOffsetMinutes) { viewModel.setPrayerOffset("Fajr", it) }
                        PrayerOffsetRow("Dhuhr", settings.dhuhrOffsetMinutes) { viewModel.setPrayerOffset("Dhuhr", it) }
                        PrayerOffsetRow("Asr", settings.asrOffsetMinutes) { viewModel.setPrayerOffset("Asr", it) }
                        PrayerOffsetRow("Maghrib", settings.maghribOffsetMinutes) { viewModel.setPrayerOffset("Maghrib", it) }
                        PrayerOffsetRow("Isha", settings.ishaOffsetMinutes) { viewModel.setPrayerOffset("Isha", it) }
                    }
                }

                ExpandableSettingsCard(
                    title = "Diagnostics",
                    summary = "Setup, coordinates, and notification status.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
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
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Debug Controls",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            Text("Notify", fontFamily = BeVietnamPro)
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
                            Icon(Icons.Default.BugReport, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pre-notify", fontFamily = BeVietnamPro)
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "Shift by minutes",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = BeVietnamPro),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { onValueChange(value - 1) }) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = if (value > 0) "+$value" else "$value",
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = { onValueChange(value + 1) }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
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
        shape = RoundedCornerShape(20.dp),
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
        SettingsToggleItem(
            title = "Show on surfaces",
            subtitle = "Keep ${preference.label} visible in Khushu.",
            checked = preference.selected,
            onCheckedChange = onSelectedToggle
        )
        SettingsToggleItem(
            title = "Notification alert",
            subtitle = "Send a reminder when ${preference.label} starts.",
            checked = preference.notificationEnabled,
            onCheckedChange = onNotificationToggle,
            showDivider = false
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
        shape = RoundedCornerShape(20.dp),
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
        SettingsToggleItem(
            title = "Prayer notification",
            subtitle = "Alert when ${preference.prayerName} starts.",
            checked = preference.prayerEnabled,
            onCheckedChange = onPrayerToggle
        )
        SettingsToggleItem(
            title = "Pre-prayer notification",
            subtitle = "Remind me before ${preference.prayerName}.",
            checked = preference.prePrayerEnabled,
            onCheckedChange = onPrePrayerToggle,
            showDivider = preference.prePrayerEnabled
        )
        if (preference.prePrayerEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lead time", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro))
                    Text(
                        text = "${preference.prePrayerMinutes} minutes before",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onPrePrayerMinutesChange(preference.prePrayerMinutes - 1) }) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "${preference.prePrayerMinutes}m",
                        modifier = Modifier.width(40.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = { onPrePrayerMinutesChange(preference.prePrayerMinutes + 1) }) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Setup", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
        DiagnosticRow("Source", source)
        DiagnosticRow("Method", method)
        DiagnosticRow("Madhab", madhab)
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
        
        Text("Location", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
        DiagnosticRow("Coordinates", "${"%.4f".format(Locale.US, latitude)}, ${"%.4f".format(Locale.US, longitude)}")
        DiagnosticRow("GPS Status", if (gpsEnabled) "Enabled" else "Disabled")
        DiagnosticRow("Last Refresh", lastRefreshed)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

        Text("Status", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
        DiagnosticRow("Notifications", if (notificationsAllowed) "Allowed" else "Not allowed")
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun ManualLocationInput(
    lat: Float,
    lng: Float,
    onLocationSave: (Float, Float) -> Unit
) {
    var latText by remember(lat) { mutableStateOf(lat.toString()) }
    var lngText by remember(lng) { mutableStateOf(lng.toString()) }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it },
                label = { Text("Latitude", fontFamily = BeVietnamPro) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro)
            )
            OutlinedTextField(
                value = lngText,
                onValueChange = { lngText = it },
                label = { Text("Longitude", fontFamily = BeVietnamPro) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = BeVietnamPro)
            )
        }
        FilledTonalButton(
            onClick = {
                val parsedLat = latText.toFloatOrNull()
                val parsedLng = lngText.toFloatOrNull()
                if (parsedLat != null && parsedLng != null) {
                    onLocationSave(parsedLat, parsedLng)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Coordinates", fontFamily = BeVietnamPro)
        }
    }
}
