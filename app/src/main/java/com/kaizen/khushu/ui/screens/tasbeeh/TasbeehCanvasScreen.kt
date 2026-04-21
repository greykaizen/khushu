package com.kaizen.khushu.ui.screens.tasbeeh

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.model.TasbeehCanvasPresetDomain
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.KhushuColors

@Composable
fun TasbeehCanvasScreen(
    viewModel: TasbeehCanvasViewModel,
    onExit: () -> Unit,
) {
    val view = LocalView.current
    val window = (LocalContext.current as Activity).window
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    val workingWidgets by viewModel.workingWidgets.collectAsStateWithLifecycle()
    val workingBackground by viewModel.workingBackgroundColor.collectAsStateWithLifecycle()
    val selectedWidgetId by viewModel.selectedWidgetId.collectAsStateWithLifecycle()
    val isUiVisible by viewModel.isUiVisible.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()

    var showAddMenu by remember { mutableStateOf(false) }
    var showPresetsMenu by remember { mutableStateOf(false) }
    var showBackgroundMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(workingBackground.toLong()))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (selectedWidgetId != null) {
                        viewModel.selectWidget(null)
                        viewModel.showUi()
                    } else {
                        viewModel.toggleUiVisibility()
                    }
                })
            },
        contentAlignment = Alignment.TopStart
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(screenWidth, screenHeight) {
            viewModel.setCanvasSize(screenWidth, screenHeight)
        }

        workingWidgets.sortedBy { it.zIndex }.forEach { widget ->
            TasbeehCanvasWidgetItem(
                widget = widget,
                isSelected = widget.id == selectedWidgetId,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onUpdate = { viewModel.updateWidget(it) },
                onTap = {
                    viewModel.selectWidget(widget.id)
                    viewModel.showUi()
                },
                onSizeMeasured = { id, w, h -> viewModel.updateWidgetSize(id, w, h) }
            )
        }

        // Action Buttons
        AnimatedVisibility(
            visible = isUiVisible && selectedWidgetId == null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExit, colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))) {
                    Text("Exit")
                }
                Button(
                    onClick = {
                        viewModel.saveLayout()
                        android.widget.Toast.makeText(context, "Tasbeeh Layout Saved", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }

        // Bottom Menu
        AnimatedVisibility(
            visible = isUiVisible && selectedWidgetId == null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorMenuAction(icon = Icons.Default.Add, label = "Add", onClick = { showAddMenu = true })
                    EditorMenuAction(icon = Icons.Default.Style, label = "Presets", onClick = { showPresetsMenu = true })
                    EditorMenuAction(icon = Icons.Default.Palette, label = "BG", onClick = { showBackgroundMenu = true })
                    EditorMenuAction(icon = Icons.Default.RestartAlt, label = "Reset", onClick = { viewModel.resetToDefault() })
                }
            }
        }

        if (showAddMenu) {
            AddTasbihWidgetSheet(
                onAdd = { widget ->
                    viewModel.addNewWidgetFromMenu(widget)
                    showAddMenu = false
                },
                onDismiss = { showAddMenu = false }
            )
        }

        if (showPresetsMenu) {
            TasbihPresetsSheet(
                presets = presets,
                onLoad = { viewModel.loadPreset(it) },
                onSave = { viewModel.saveAsPreset(it) },
                onDelete = { viewModel.deletePreset(it) },
                onDismiss = { showPresetsMenu = false }
            )
        }

        if (showBackgroundMenu) {
            TasbihBackgroundSheet(
                currentColor = Color(workingBackground.toLong()),
                onSelect = { viewModel.updateBackgroundColor(it.toArgb()) },
                onDismiss = { showBackgroundMenu = false }
            )
        }

        // Selection / Config
        selectedWidgetId?.let { id ->
            workingWidgets.find { it.id == id }?.let { selectedWidget ->
                TasbihWidgetConfigSheet(
                    widget = selectedWidget,
                    onUpdate = { viewModel.updateWidget(it) },
                    onDelete = { viewModel.removeWidget(id) },
                    onDismiss = { viewModel.selectWidget(null) },
                    onAlign = { h, v -> viewModel.alignSelectedWidget(h, v) },
                )
            }
        }
    }
}

@Composable
private fun EditorMenuAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun TasbeehCanvasWidgetItem(
    widget: TasbihWidget,
    isSelected: Boolean,
    screenWidth: Float,
    screenHeight: Float,
    onUpdate: (TasbihWidget) -> Unit,
    onTap: () -> Unit,
    onSizeMeasured: (String, Float, Float) -> Unit,
) {
    val currentWidget by rememberUpdatedState(widget)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnSizeMeasured by rememberUpdatedState(onSizeMeasured)

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = (widget.offsetX * screenWidth) - (size.width / 2f)
                translationY = (widget.offsetY * screenHeight) - (size.height / 2f)
                scaleX = widget.scale
                scaleY = widget.scale
                transformOrigin = TransformOrigin.Center
            }
            .then(
                if (isSelected) Modifier.border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                else Modifier
            )
            .onGloballyPositioned { coordinates ->
                currentOnSizeMeasured(
                    widget.id,
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            }
            .pointerInput(widget.id) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(widget.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val w = currentWidget
                    val newOffsetX = (w.offsetX + (pan.x / screenWidth)).coerceIn(0f, 1f)
                    val newOffsetY = if (w is TasbihWidget.StringBeadWidget) 0.5f 
                                    else (w.offsetY + (pan.y / screenHeight)).coerceIn(0f, 1f)
                    val newScale = (w.scale * zoom).coerceIn(0.2f, 5f)

                    val updated = when (w) {
                        is TasbihWidget.StringBeadWidget -> w.copy(offsetX = newOffsetX, offsetY = newOffsetY, scale = newScale)
                        is TasbihWidget.DhikrNameWidget -> w.copy(offsetX = newOffsetX, offsetY = newOffsetY, scale = newScale)
                        is TasbihWidget.CounterWidget -> w.copy(offsetX = newOffsetX, offsetY = newOffsetY, scale = newScale)
                        is TasbihWidget.ProgressCircleWidget -> w.copy(offsetX = newOffsetX, offsetY = newOffsetY, scale = newScale)
                        is TasbihWidget.MeaningWidget -> w.copy(offsetX = newOffsetX, offsetY = newOffsetY, scale = newScale)
                    }
                    currentOnUpdate(updated)
                }
            }
    ) {
        val renderHeight = if (widget is TasbihWidget.StringBeadWidget) screenHeight else 0f
        
        Box(modifier = if (renderHeight > 0) Modifier.height(with(androidx.compose.ui.platform.LocalDensity.current) { renderHeight.toDp() }) else Modifier) {
            TasbihWidgetRenderer(
                widget = widget,
                currentCount = 33,
                currentItem = null,
                stringControlXOffset = 0f,
                stringControlYFraction = 0.5f,
                countedBeads = 12,
                totalBeads = 33,
                beadStyle = BeadStyle.CLASSIC_AMBER,
                activeBeadProgress = null,
                thumbPosition = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTasbihWidgetSheet(
    onAdd: (TasbihWidget) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "ADD TASBIH WIDGET",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetPreviewCard(
                    title = "Counter",
                    modifier = Modifier.weight(1f),
                    onClick = { onAdd(TasbihWidget.CounterWidget(id = "counter_${System.currentTimeMillis()}")) }
                ) {
                    Text("33", fontSize = 40.sp, color = Color.White, fontFamily = Antonio)
                }
                WidgetPreviewCard(
                    title = "Dhikr Name",
                    modifier = Modifier.weight(1f),
                    onClick = { onAdd(TasbihWidget.DhikrNameWidget(id = "name_${System.currentTimeMillis()}")) }
                ) {
                    Text("سبحان الله", fontSize = 18.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            WidgetPreviewCard(
                title = "Bead String",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAdd(TasbihWidget.StringBeadWidget(id = "string_${System.currentTimeMillis()}")) }
            ) {
                Box(Modifier.width(2.dp).fillMaxHeight(0.6f).background(Color.White.copy(alpha = 0.3f)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihPresetsSheet(
    presets: List<TasbeehCanvasPresetDomain>,
    onLoad: (TasbeehCanvasPresetDomain) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Layout Presets",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { showSaveDialog = true }, shape = RoundedCornerShape(8.dp)) {
                    Text("Save New")
                }
            }
            Spacer(Modifier.height(24.dp))

            if (presets.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No custom presets yet", color = Color.White.copy(alpha = 0.4f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presets) { preset ->
                        OutlinedCard(
                            onClick = { onLoad(preset); onDismiss() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(preset.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    Text("${preset.widgets.size} widgets", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                                    Spacer(Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Delete, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp).clickable { onDelete(preset.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Preset") },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") }
                )
            },
            confirmButton = {
                Button(onClick = { onSave(presetName); showSaveDialog = false }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihBackgroundSheet(
    currentColor: Color,
    onSelect: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = listOf(Color.Black, Color(0xFF1A1A1A), Color(0xFF0D1B2A), Color(0xFF1B4332), Color(0xFF432818))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 16.dp)) {
            Text("Canvas Background", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(2.dp, if (currentColor == color) Color.White else Color.Transparent, CircleShape)
                            .clickable { onSelect(color) }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetPreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    previewContent: @Composable BoxScope.() -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
                content = previewContent
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(text = title, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihWidgetConfigSheet(
    widget: TasbihWidget,
    onUpdate: (TasbihWidget) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onAlign: (Alignment.Horizontal?, Alignment.Vertical?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Configure Widget",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, null)
                }
            }
            Spacer(Modifier.height(24.dp))

            SliderControl("Scale", widget.scale, 0.5f, 3f) { onUpdate(
                when(widget) {
                    is TasbihWidget.StringBeadWidget -> widget.copy(scale = it)
                    is TasbihWidget.DhikrNameWidget -> widget.copy(scale = it)
                    is TasbihWidget.CounterWidget -> widget.copy(scale = it)
                    is TasbihWidget.ProgressCircleWidget -> widget.copy(scale = it)
                    is TasbihWidget.MeaningWidget -> widget.copy(scale = it)
                }
            )}

            Spacer(Modifier.height(24.dp))
            Text("Alignment", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Alignment.Start to Icons.AutoMirrored.Filled.AlignHorizontalLeft,
                    Alignment.CenterHorizontally to Icons.Default.AlignHorizontalCenter,
                    Alignment.End to Icons.AutoMirrored.Filled.AlignHorizontalRight
                ).forEach { (align, icon) ->
                    FilledTonalButton(onClick = { onAlign(align, null) }, modifier = Modifier.weight(1f)) {
                        Icon(icon, null)
                    }
                }
            }
            
            if (widget !is TasbihWidget.StringBeadWidget) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Alignment.Top to Icons.Default.VerticalAlignTop,
                        Alignment.CenterVertically to Icons.Default.VerticalAlignCenter,
                        Alignment.Bottom to Icons.Default.VerticalAlignBottom
                    ).forEach { (align, icon) ->
                        FilledTonalButton(onClick = { onAlign(null, align) }, modifier = Modifier.weight(1f)) {
                            Icon(icon, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderControl(label: String, value: Float, min: Float, max: Float, onUpdate: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value = value, onValueChange = onUpdate, valueRange = min..max)
    }
}
