package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
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

            SectionHeader("General")
            SettingsToggle(
                title = "Keep Screen Awake",
                subtitle = "Prevent device sleep during sessions",
                checked = settings.keepScreenAwake,
                onCheckedChange = { viewModel.toggleKeepScreenAwake(it) }
            )

            Spacer(Modifier.height(16.dp))

            MenuSectionItem(
                title = "Counter",
                detail = "Haptics, Volume Keys & Feedback",
                imageVector = Icons.Default.TouchApp,
                onClick = onNavigateCounter
            )

            MenuSectionItem(
                title = "Appearance",
                detail = "Theme, Dynamic Color & AMOLED",
                imageVector = Icons.Default.ColorLens,
                onClick = onNavigateAppearance
            )

            MenuSectionItem(
                title = "Prayer Times",
                detail = "Calculation Method & Asr Rules",
                imageVector = Icons.Default.AccessTime,
                onClick = onNavigatePrayer
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
