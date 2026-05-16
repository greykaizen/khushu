package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.kaizen.khushu.ui.screens.tasbeeh.TasbihBeadCustomizerSheet
import com.kaizen.khushu.ui.screens.tasbeeh.TasbihSoundCatalog
import com.kaizen.khushu.ui.screens.tasbeeh.TasbihSoundPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehCustomizeScreen(
    viewModel: SettingsViewModel,
    onPreview: () -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val soundPlayer = remember { TasbihSoundPlayer(context) }
    var showBeadSheet by remember { mutableStateOf(false) }

    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.release() }
    }

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
            Spacer(Modifier.height(12.dp))

            SettingsGroup(
                title = "Layout",
//                description = "Open the editor or change how beads are styled."
            ) {
                SettingsMenuItem(
                    title = "Edit Tasbih Screen",
                    subtitle = "Customize layout and widget placement",
                    onClick = onPreview
                )
                SettingsMenuItem(
                    title = "Design Tasbih Beads",
                    subtitle = "Material, shape, and finish",
                    onClick = { showBeadSheet = true },
                    showDivider = false
                )
            }

            SettingsGroup(
                title = "Behavior",
//                description = "How the Tasbih screen reacts while you count."
            ) {
                SettingsToggleItem(
                    title = "Dynamic Colors",
                    subtitle = "Use collection colors automatically.",
                    checked = settings.tasbeehDynamicColors,
                    onCheckedChange = { viewModel.toggleTasbeehDynamicColors(it) }
                )
                SettingsToggleItem(
                    title = "Stealth Mode",
                    subtitle = "Allow hiding widgets for a cleaner view.",
                    checked = settings.tasbeehStealthModeAllowed,
                    onCheckedChange = { viewModel.toggleTasbeehStealthModeAllowed(it) }
                )
                SettingsToggleItem(
                    title = "Volume Buttons",
                    subtitle = "Use physical volume keys to count.",
                    checked = settings.tasbeehVolumeEnabled,
                    onCheckedChange = { viewModel.toggleTasbeehVolumeEnabled(it) },
                    showDivider = settings.tasbeehVolumeEnabled
                )
                if (settings.tasbeehVolumeEnabled) {
                    SettingsToggleItem(
                        title = "Animate Volume Keys",
                        subtitle = "Show bead movement when counting.",
                        checked = settings.tasbeehVolumeAnimation,
                        onCheckedChange = { viewModel.toggleTasbeehVolumeAnimation(it) },
                        showDivider = false
                    )
                }
            }

            SettingsGroup(
                title = "Sound",
//                description = "Bundled Tasbih sounds that play when a count lands."
            ) {
                SettingsToggleItem(
                    title = "Tasbih Sound",
                    subtitle = "Play a soft sound when a bead count commits.",
                    checked = settings.tasbihSoundEnabled,
                    onCheckedChange = { viewModel.toggleTasbihSoundEnabled(it) },
                    showDivider = settings.tasbihSoundEnabled
                )
                if (settings.tasbihSoundEnabled) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        SettingsDropdown(
                            title = "Sound Style",
                            subtitle = "Choose the bundled Tasbih sound.",
                            options = TasbihSoundCatalog.options.map { it.id },
                            selectedOption = settings.tasbihSoundId,
                            optionLabel = { id -> TasbihSoundCatalog.optionFor(id).label },
                            onOptionSelected = viewModel::setTasbihSoundId
                        )
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = { soundPlayer.play(settings.tasbihSoundId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Preview Sound")
                        }
                    }
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
