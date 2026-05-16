package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TouchApp
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
            Spacer(Modifier.height(12.dp))

            SettingsGroup(
                title = "Preferences",
//                description = "Prayer, appearance, interaction, and app information."
            ) {
                SettingsMenuItem(
                    title = "Prayer Times",
                    subtitle = "Calculation, reminders, and diagnostics",
                    imageVector = Icons.Default.AccessTime,
                    onClick = onNavigatePrayer
                )

                SettingsMenuItem(
                    title = "Appearance",
                    subtitle = "Theme, colors, and startup screen",
                    imageVector = Icons.Default.ColorLens,
                    onClick = onNavigateAppearance
                )

                SettingsMenuItem(
                    title = "Counter",
                    subtitle = "Haptics and hardware behavior",
                    imageVector = Icons.Default.TouchApp,
                    onClick = onNavigateCounter
                )

                SettingsMenuItem(
                    title = "About Khushu",
                    subtitle = "Story, project links, and reporting",
                    imageVector = Icons.Default.Info,
                    onClick = onNavigateAbout,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
