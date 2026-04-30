package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.kaizen.khushu.ui.screens.tasbeeh.TasbihBeadCustomizerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehCustomizeScreen(
    viewModel: SettingsViewModel,
    onPreview: () -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showBeadSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Tasbih Screen", scrollBehavior) },
                navigationIcon = { SettingsBackButton(onBack) },
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsSectionCard(
                title = "Layout",
                subtitle = "Open the editor or change how beads are styled."
            ) {
                MenuSectionItem(
                    title = "Edit Tasbih Screen",
                    detail = "Customize your counter layout and widget placement",
                    onClick = onPreview
                )
                MenuSectionItem(
                    title = "Design Tasbih Beads",
                    detail = "Design the active bead material, shape, and finish",
                    onClick = { showBeadSheet = true }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionCard(
                title = "Behavior",
                subtitle = "How the Tasbih screen reacts while you count."
            ) {
                SettingsToggle(
                    title = "Dynamic Colors",
                    subtitle = "Use collection colors automatically in the list and counter.",
                    checked = settings.tasbeehDynamicColors,
                    onCheckedChange = { viewModel.toggleTasbeehDynamicColors(it) }
                )
                SettingsToggle(
                    title = "Stealth Mode",
                    subtitle = "Allow hiding widgets for a cleaner private counting view.",
                    checked = settings.tasbeehStealthModeAllowed,
                    onCheckedChange = { viewModel.toggleTasbeehStealthModeAllowed(it) }
                )
                SettingsToggle(
                    title = "Volume Buttons",
                    subtitle = "Use physical volume keys to count.",
                    checked = settings.tasbeehVolumeEnabled,
                    onCheckedChange = { viewModel.toggleTasbeehVolumeEnabled(it) }
                )
                if (settings.tasbeehVolumeEnabled) {
                    SettingsToggle(
                        title = "Animate Volume Keys",
                        subtitle = "Show bead movement when counting with the hardware buttons.",
                        checked = settings.tasbeehVolumeAnimation,
                        onCheckedChange = { viewModel.toggleTasbeehVolumeAnimation(it) }
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showBeadSheet) {
        TasbihBeadCustomizerSheet(
            settingsViewModel = viewModel,
            onDismiss = { showBeadSheet = false }
        )
    }
}
