package com.kaizen.khushu.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahCustomizeScreen(
    viewModel: SettingsViewModel,
    onCustomizeLayout: (Int) -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Pray Screen", scrollBehavior) },
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
                title = "Layout",
//                description = "Edit the custom Pray layout used by the immersive screen."
            ) {
                Box(modifier = Modifier.padding(3.dp)) {
                    Surface(
                        onClick = { onCustomizeLayout(4) },
                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(24.dp),
//                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
//                        border = androidx.compose.foundation.BorderStroke(
//                            1.dp,
//                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
//                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Edit Pray Screen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Customize the immersive layout preset.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = BeVietnamPro,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            SettingsGroup(
                title = "Session",
//                description = "What appears while you move through a guided prayer."
            ) {
                SettingsToggleItem(
                    title = "Show Exit Button",
                    subtitle = "Keep an exit control visible during sessions.",
                    checked = settings.showExitButton,
                    onCheckedChange = { viewModel.updateShowExitButton(it) }
                )
                SettingsToggleItem(
                    title = "Show Completion Text",
                    subtitle = "Display a message when the target is reached.",
                    checked = settings.showCompletionText,
                    onCheckedChange = { viewModel.updateShowCompletionText(it) }
                )

                AnimatedVisibility(visible = settings.showCompletionText) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        OutlinedTextField(
                            value = if (settings.completionText == "الحمد لله") "" else settings.completionText,
                            onValueChange = { text -> viewModel.updateCompletionText(text) },
                            label = { Text("Completion Message", fontFamily = BeVietnamPro) },
                            placeholder = {
                                Text("الحمد لله", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontFamily = BeVietnamPro)
                            },
                            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                                fontFamily = BeVietnamPro,
                                textDirection = androidx.compose.ui.text.style.TextDirection.Content
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                SettingsToggleItem(
                    title = "Show Step Timer",
                    subtitle = "Display elapsed time for each prayer step.",
                    checked = settings.showStepTimer,
                    onCheckedChange = { viewModel.toggleShowStepTimer(it) },
                    showDivider = false
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
