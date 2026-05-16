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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.navigation.AppDestinations
import androidx.compose.ui.text.font.FontWeight
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
    val buttonCoordinates = remember { mutableStateListOf<LayoutCoordinates?>(null, null, null) }
    val themeOptions = listOf("System", "Light", "Dark")
    val themeIcons = listOf(
        Icons.Default.Settings,
        Icons.Default.WbSunny,
        Icons.Default.DarkMode
    )
    val isDarkTheme = when (settings.themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

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
            Spacer(Modifier.height(12.dp))

            SettingsGroup(
                title = "Theme",
//                description = "Control the overall look and startup behavior of Khushu."
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                modifier = Modifier.onGloballyPositioned { buttonCoordinates[index] = it },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                                onClick = {
                                    val center = buttonCoordinates[index]?.boundsInRoot()?.center ?: Offset.Zero
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
                                label = {
                                    Text(
                                        text = label,
                                        fontFamily = BeVietnamPro,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            )
                        }
                    }
                }

                SettingsToggleItem(
                    title = "Show Continue Reading",
                    subtitle = "Keep the last opened study topic visible in Study.",
                    checked = settings.showContinueReading,
                    onCheckedChange = viewModel::toggleShowContinueReading
                )

                SettingsToggleItem(
                    title = "Keep Screen Awake",
                    subtitle = "Prevent the screen from sleeping while Khushu is open.",
                    checked = settings.keepScreenAwake,
                    onCheckedChange = viewModel::toggleKeepScreenAwake
                )

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Startup screen",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose which area opens first when Khushu launches.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BeVietnamPro),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Home" to AppDestinations.HOME.route,
                            "Pray" to AppDestinations.SALAH.route,
                            "Tasbih" to AppDestinations.TASBEEH.route,
                            "Study" to AppDestinations.LEARN.route
                        ).forEach { (label, route) ->
                            FilterChip(
                                selected = settings.startupTab == route,
                                onClick = { viewModel.setStartupTab(route) },
                                label = { Text(label, fontFamily = BeVietnamPro) }
                            )
                        }
                    }
                }
            }

            SettingsGroup(
                title = "Color",
//                description = "Use wallpaper-aware colors or choose a manual accent."
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsToggleItem(
                        title = "Dynamic Color",
                        subtitle = "Sync the accent palette with your wallpaper.",
                        checked = settings.dynamicColor,
                        onCheckedChange = viewModel::toggleDynamicColor
                    )
                }

                if (isDarkTheme) {
                    SettingsToggleItem(
                        title = "Pure AMOLED Black",
                        subtitle = "Use an absolute black background in dark mode.",
                        checked = settings.pureBlack,
                        onCheckedChange = viewModel::togglePureBlack,
                        showDivider = !settings.dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    )
                }

                AnimatedVisibility(
                    visible = !settings.dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
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
        if (color == colorSeeds["default"]) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

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
