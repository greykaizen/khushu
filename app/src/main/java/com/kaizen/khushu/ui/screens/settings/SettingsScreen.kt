package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateCounter: () -> Unit,
    onNavigateAppearance: () -> Unit,
    onNavigatePrayer: () -> Unit,
    onNavigateAbout: () -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Settings", scrollBehavior) },
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
                title = "App behavior",
                subtitle = "Core behavior that applies across Khushu."
            ) {
                SettingsToggle(
                    title = "Keep Screen Awake",
                    subtitle = "Prevent device sleep during prayer and tasbih sessions.",
                    checked = settings.keepScreenAwake,
                    onCheckedChange = { viewModel.toggleKeepScreenAwake(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionCard(
                title = "Preferences",
                subtitle = "Timing, feedback, appearance, and app information."
            ) {
                MenuSectionItem(
                    title = "Prayer Times",
                    detail = "Calculation, reminders, Home visibility, and diagnostics",
                    imageVector = Icons.Default.AccessTime,
                    onClick = onNavigatePrayer
                )

                MenuSectionItem(
                    title = "Appearance",
                    detail = "Theme, colors, startup screen, and reading defaults",
                    imageVector = Icons.Default.ColorLens,
                    onClick = onNavigateAppearance
                )

                MenuSectionItem(
                    title = "Counter",
                    detail = "Haptics and hardware counting behavior",
                    imageVector = Icons.Default.TouchApp,
                    onClick = onNavigateCounter
                )

                MenuSectionItem(
                    title = "About Khushu",
                    detail = "Story, project links, and issue reporting",
                    imageVector = Icons.Default.Info,
                    onClick = onNavigateAbout
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
