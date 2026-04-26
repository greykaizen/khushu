package com.kaizen.khushu.ui.screens.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.components.KhushuAppBar
import dev.chrisbanes.haze.HazeState
import java.util.concurrent.TimeUnit

private fun findNextPrayer(prayers: List<PrayerInfo>, now: Long): PrayerInfo? {
    if (prayers.isEmpty()) return null

    prayers.firstOrNull { it.rawTime > now }?.let { return it }

    val fajr = prayers.first()
    return fajr.copy(rawTime = fajr.rawTime + TimeUnit.DAYS.toMillis(1))
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hazeState: HazeState,
    contentPadding: PaddingValues,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isSystemInDarkTheme()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var doneStates by remember {
        mutableStateOf(
            mapOf(
                "Fajr" to true,
                "Dhuhr" to false,
                "Asr" to false,
                "Maghrib" to false,
                "Isha" to false
            )
        )
    }
    var showTimeOverrideDialog by remember { mutableStateOf(false) }
    var previewHourText by remember { mutableStateOf("") }
    var previewMinuteText by remember { mutableStateOf("") }

    val nextPrayer = findNextPrayer(uiState.prayers, uiState.currentTimeMillis)
    val doneCount = doneStates.values.count { it }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshPrayerData() }
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + 28.dp,
                    bottom = 0.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NextPrayerCard(
                            prayer = nextPrayer,
                            doneCount = doneCount,
                            source = uiState.calculationSource,
                            usingPreviewTime = uiState.usingPreviewTime,
                            onTimeClick = {
                                showTimeOverrideDialog = true
                                previewHourText = ""
                                previewMinuteText = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SunArcCard(
                            sunT = uiState.sunArcT,
                            nextT = nextPrayer?.arcT,
                            nextName = nextPrayer?.name.orEmpty(),
                            makruhZones = uiState.makruhZones,
                            darkTheme = darkTheme,
                            hijriDate = uiState.hijriDate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                item {
                    EventsStrip(events = uiState.events)
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                item {
                    PrayerSlab(
                        prayers = uiState.prayers,
                        doneStates = doneStates,
                        onToggleDone = { name ->
                            doneStates = doneStates.toMutableMap().apply {
                                this[name] = !(this[name] ?: false)
                            }
                        },
//                    ayahText = uiState.ayahText,
                        ayahRef = uiState.ayahRef,
                        darkTheme = darkTheme,
                        bottomPadding = contentPadding.calculateBottomPadding()
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            )
        }

        KhushuAppBar(
            title = "Khushu", // Antonio font handled internally
            onSettingsClick = onSettingsClick,
//            hazeState = hazeState, // Unused in newer version of KhushuAppBar
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }

    if (showTimeOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showTimeOverrideDialog = false },
            title = { Text("Preview Prayer Time") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set a temporary 24-hour clock time to test next prayer and makruh transitions.")
                    OutlinedTextField(
                        value = previewHourText,
                        onValueChange = { previewHourText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hour (0-23)") }
                    )
                    OutlinedTextField(
                        value = previewMinuteText,
                        onValueChange = { previewMinuteText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Minute (0-59)") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = previewHourText.toIntOrNull()
                        val minute = previewMinuteText.toIntOrNull()
                        if (hour != null && minute != null) {
                            viewModel.setPreviewTime(hour, minute)
                            showTimeOverrideDialog = false
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearPreviewTime()
                            showTimeOverrideDialog = false
                        }
                    ) {
                        Text("Reset")
                    }
                    TextButton(onClick = { showTimeOverrideDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}
