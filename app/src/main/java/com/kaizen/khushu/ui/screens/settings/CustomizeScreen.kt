package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.components.KhushuLogoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateSalah: () -> Unit,
    onNavigateTasbeeh: () -> Unit,
    onBack: () -> Unit
) {
    val settings by settingsViewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Customize", scrollBehavior) },
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
                title = "Prayer experience",
                subtitle = "Tune the guided prayer screen and tasbih counter behavior."
            ) {
                MenuSectionItem(
                    title = "Pray Screen",
                    detail = "Session controls, completion text, and layout editor",
                    iconRes = com.kaizen.khushu.R.drawable.ic_salah,
                    onClick = onNavigateSalah
                )

                MenuSectionItem(
                    title = "Tasbih Screen",
                    detail = "Layout editor, bead style, and interaction behavior",
                    iconRes = com.kaizen.khushu.R.drawable.ic_tasbeeh,
                    onClick = onNavigateTasbeeh
                )
            }

            Spacer(Modifier.height(20.dp))

            SettingsSectionCard(
                title = "App Icon",
                subtitle = "Choose how Khushu appears on your launcher."
            ) {
                LogoStyleGrid(
                    selected = settings.logoStyle,
                    onSelect = { settingsViewModel.setLogoStyle(it) },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private data class LogoStyleOption(val key: String, val label: String)

private val logoStyleOptions = listOf(
    LogoStyleOption("DYNAMIC", "Dynamic"),
    LogoStyleOption("DARK",    "Dark"),
    LogoStyleOption("LIGHT",   "Light"),
    LogoStyleOption("GREEN",   "Islamic Green"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogoStyleGrid(
    selected: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2,
    ) {
        logoStyleOptions.forEach { option ->
            LogoStyleCard(
                option = option,
                isSelected = selected == option.key,
                onClick = { onSelect(option.key) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LogoStyleCard(
    option: LogoStyleOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KhushuLogoBadge(
            logoStyle = option.key,
            size = 56.dp,
            iconSize = 36.dp,
        )
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
        )
    }
}
