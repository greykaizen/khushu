package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehCustomizeScreen(
    viewModel: SettingsViewModel,
    onPreview: () -> Unit = {},
    onCustomizeBeads: () -> Unit = {},
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Tasbeeh Visuals", scrollBehavior) },
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
            SectionHeader("Preview")
            MenuSectionItem(
                title = "Preview Physical Screen",
                detail = "See the new tasbih bead counter",
                onClick = onPreview
            )
            MenuSectionItem(
                title = "Bead Style",
                detail = "Choose Classic Amber or Dark Onyx",
                onClick = onCustomizeBeads
            )
            Spacer(Modifier.height(16.dp))
            SectionHeader("Interface")
            SettingsToggle(
                title = "Vibrate on Count",
                subtitle = "Feel a subtle vibration on each tap",
                checked = settings.vibrationOnCount,
                onCheckedChange = { viewModel.toggleVibrationOnCount(it) }
            )
            SettingsToggle(
                title = "Show Lap Counter",
                subtitle = "Track completed sets of 33 or 99",
                checked = settings.showLapCounter,
                onCheckedChange = { viewModel.toggleShowLapCounter(it) }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
