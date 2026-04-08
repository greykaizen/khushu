package com.kaizen.khushu.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.navigation.AppDestinations
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.LocalThemeTransitionController
import com.kaizen.khushu.ui.theme.colorSeeds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val transitionController = LocalThemeTransitionController.current

    val btnCoords = remember { mutableStateListOf<LayoutCoordinates?>(null, null, null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Appearance", scrollBehavior) },
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
            SectionHeader("Theme")

            val themeOptions = listOf("System", "Light", "Dark")
            val themeIcons = listOf(
                Icons.Default.Settings,
                Icons.Default.WbSunny,
                Icons.Default.DarkMode
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                themeOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        modifier = Modifier.onGloballyPositioned { btnCoords[index] = it },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                        onClick = {
                            val center = btnCoords[index]?.boundsInRoot()?.center ?: Offset.Zero
                            transitionController.captureAndChange(center) {
                                viewModel.setThemeMode(label)
                            }
                        },
                        selected = settings.themeMode == label,
                        icon = {
                            Icon(
                                imageVector = themeIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text(label, fontFamily = BeVietnamPro, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("System")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsToggle(
                    title = "Dynamic Color",
                    subtitle = "Sync accent color with wallpaper (Material You)",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.toggleDynamicColor(it) }
                )
            }

            val isDarkTheme = when (settings.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            if (isDarkTheme) {
                SettingsToggle(
                    title = "Pure AMOLED Black",
                    subtitle = "Use absolute #000000 background",
                    checked = settings.pureBlack,
                    onCheckedChange = { viewModel.togglePureBlack(it) }
                )
            }

            AnimatedVisibility(
                visible = !settings.dynamicColor,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader("Accent Color")
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorSeeds.forEach { (key, color) ->
                            AccentColorChip(
                                color = color,
                                selected = settings.colorSeed == key,
                                onClick = { viewModel.setColorSeed(key) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("Startup Screen")

            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = "Choose which tab opens when the app launches",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "Salah" to AppDestinations.SALAH.route,
                        "Tasbeeh" to AppDestinations.TASBEEH.route,
                        "Learn" to AppDestinations.LEARN.route
                    )

                    tabs.forEach { (label, route) ->
                        FilterChip(
                            selected = settings.startupTab == route,
                            onClick = { viewModel.setStartupTab(route) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AccentColorChip(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}