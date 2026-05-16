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
fun CounterSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Counter", scrollBehavior) },
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
                title = "Haptics",
//                description = "Physical feedback that applies across Khushu counters."
            ) {
                SettingsToggleItem(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate slightly when a count increments.",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.toggleHaptics(it) },
                    showDivider = false
                )
            }

            SettingsGroup(
                title = "Hardware",
//                description = "Optional device-level controls for counting."
            ) {
                SettingsToggleItem(
                    title = "Volume Key Counting",
                    subtitle = "Use the hardware volume buttons to count.",
                    checked = settings.volumeCounting,
                    onCheckedChange = { viewModel.toggleVolumeCounting(it) },
                    showDivider = false
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
