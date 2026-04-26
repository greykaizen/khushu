package com.kaizen.khushu.ui.screens.settings

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

private fun prayerSettingLabel(value: String, labels: Map<String, String>): String {
    return labels[value] ?: value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lastRefreshed = if (settings.lastPrayerRefreshEpochMs > 0L) {
        SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(settings.lastPrayerRefreshEpochMs))
    } else {
        "Not refreshed yet"
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.refreshLocation()
        }
    }

    val calculationMethods = listOf(
        "MUSLIM_WORLD_LEAGUE", "EGYPTIAN", "KARACHI", "UMM_AL_QURA", 
        "DUBAI", "MOON_SIGHTING_COMMITTEE", "NORTH_AMERICA", "KUWAIT", 
        "QATAR", "SINGAPORE", "TEHRAN", "TURKEY"
    )
    
    val madhabs = listOf("SHAFI", "HANAFI")
    val sources = listOf("LOCAL", "API")

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
            SectionHeader("Calculation Method")
            SettingsDropdown(
                title = "Convention",
                subtitle = "The mathematical convention used to calculate Fajr and Isha.",
                options = calculationMethods,
                selectedOption = settings.prayerCalculationMethod,
                optionLabel = { prayerSettingLabel(it, calculationMethodLabels) },
                onOptionSelected = { viewModel.setPrayerCalculationMethod(it) }
            )

            Spacer(Modifier.height(16.dp))
            SettingsDropdown(
                title = "Madhab",
                subtitle = "Standard (Shafi, Maliki, Hanbali) or Hanafi.",
                options = madhabs,
                selectedOption = settings.prayerMadhab,
                optionLabel = { prayerSettingLabel(it, madhabLabels) },
                onOptionSelected = { viewModel.setPrayerMadhab(it) }
            )

            Spacer(Modifier.height(16.dp))
            SectionHeader("Calculation Source")
            SettingsDropdown(
                title = "Source",
                subtitle = "Local Formula (Offline) or AlAdhan API (Online).",
                options = sources,
                selectedOption = settings.prayerSourceType,
                optionLabel = { prayerSettingLabel(it, sourceLabels) },
                onOptionSelected = { viewModel.setPrayerSourceType(it) }
            )

            Spacer(Modifier.height(16.dp))
            SectionHeader("Location")
            SettingsToggle(
                title = "Use Device GPS",
                subtitle = "Automatically update your location for accurate times.",
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
                        permissionLauncher.launch(
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
                    Text("Request / Refresh GPS")
                }

                OutlinedButton(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh Location")
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Prayer Offsets")
            PrayerOffsetRow("Fajr", settings.fajrOffsetMinutes) { viewModel.setPrayerOffset("Fajr", it) }
            PrayerOffsetRow("Dhuhr", settings.dhuhrOffsetMinutes) { viewModel.setPrayerOffset("Dhuhr", it) }
            PrayerOffsetRow("Asr", settings.asrOffsetMinutes) { viewModel.setPrayerOffset("Asr", it) }
            PrayerOffsetRow("Maghrib", settings.maghribOffsetMinutes) { viewModel.setPrayerOffset("Maghrib", it) }
            PrayerOffsetRow("Isha", settings.ishaOffsetMinutes) { viewModel.setPrayerOffset("Isha", it) }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Diagnostics")
            PrayerDiagnosticsCard(
                source = prayerSettingLabel(settings.prayerSourceType, sourceLabels),
                method = prayerSettingLabel(settings.prayerCalculationMethod, calculationMethodLabels),
                madhab = prayerSettingLabel(settings.prayerMadhab, madhabLabels),
                latitude = settings.locationLat,
                longitude = settings.locationLng,
                lastRefreshed = lastRefreshed,
                gpsEnabled = settings.useGpsLocation
            )
            
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
private fun PrayerDiagnosticsCard(
    source: String,
    method: String,
    madhab: String,
    latitude: Float,
    longitude: Float,
    lastRefreshed: String,
    gpsEnabled: Boolean
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
            Text("Last Refresh: $lastRefreshed", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
