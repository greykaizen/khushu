package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.kaizen.khushu.ui.theme.BeVietnamPro
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
            Spacer(Modifier.height(12.dp))

            SettingsGroup(
                title = "Prayer experience",
//                description = "Tune the guided prayer screen and tasbih counter behavior."
            ) {
                SettingsMenuItem(
                    title = "Pray Screen",
                    subtitle = "Session controls, completion text, and layout editor",
                    iconRes = com.kaizen.khushu.R.drawable.ic_salah,
                    onClick = onNavigateSalah
                )

                SettingsMenuItem(
                    title = "Tasbih Screen",
                    subtitle = "Layout editor, bead style, and interaction behavior",
                    iconRes = com.kaizen.khushu.R.drawable.ic_tasbeeh,
                    onClick = onNavigateTasbeeh,
                    showDivider = false
                )
            }

            SettingsGroup(
                title = "App Icon",
//                description = "Choose how Khushu appears on your launcher."
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    LogoStyleGrid(
                        selected = settings.logoStyle,
                        onSelect = { settingsViewModel.setLogoStyle(it) },
                    )
                }
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
    val shape = RoundedCornerShape(24.dp)
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        Color.Transparent
    
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(if (isSelected) 2.dp else 0.dp, borderColor, shape)
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
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = BeVietnamPro),
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
        )
    }
}
