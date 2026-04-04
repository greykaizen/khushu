package com.kaizen.khushu.ui.screens.salah

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.input.pointer.pointerInput
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.ColorFilter
import androidx.core.view.WindowCompat
import com.kaizen.khushu.ui.theme.BeVietnamPro
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.CanvasWidget
import com.kaizen.khushu.data.CanvasPreset
import com.kaizen.khushu.data.DefaultPresets
import com.kaizen.khushu.data.resolveFontFamily
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.KhushuColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class WidgetData(
    val opacity: Float,
    val isOutline: Boolean,
    val color: Int,
    val fontSize: Float,
    val fontWeight: Int,
    val text: String?,
    val italic: Boolean,
    val textAlign: String,
    val verticalAlign: String
)

@Composable
fun SalahCanvasScreen(
    targetRakats: Int,
    viewModel: SalahCanvasViewModel,
    onSave: () -> Unit,
    onExit: () -> Unit,
) {
    val view = LocalView.current
    val window = (LocalContext.current as Activity).window
    DisposableEffect(Unit) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val workingWidgets by viewModel.workingWidgets.collectAsStateWithLifecycle()
    val workingBackground by viewModel.workingBackgroundColor.collectAsStateWithLifecycle()
    val selectedWidgetId by viewModel.selectedWidgetId.collectAsStateWithLifecycle()
    val isUiVisible by viewModel.isUiVisible.collectAsStateWithLifecycle()

    val layout by viewModel.layout.collectAsStateWithLifecycle()

    val canvasBackgroundColor = Color(workingBackground.toLong())

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBackgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (selectedWidgetId != null) {
                            viewModel.selectWidget(null)
                            viewModel.showUi()
                        } else {
                            viewModel.toggleUiVisibility()
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val context = LocalContext.current

        // Update canvas size for alignment calculations
        LaunchedEffect(screenWidth, screenHeight) {
            viewModel.setCanvasSize(screenWidth, screenHeight)
        }

        workingWidgets.sortedBy { it.zIndex }.forEach { widget ->
            CanvasWidgetItem(
                widget = widget,
                isSelected = widget.id == selectedWidgetId,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                currentRakats = targetRakats,
                onUpdate = { viewModel.updateWidget(it) },
                onTap = {
                    viewModel.selectWidget(widget.id)
                    viewModel.showUi()
                },
                onSizeMeasured = { id, w, h -> viewModel.updateWidgetSize(id, w, h) }
            )
        }

        // Action Buttons (Save/Exit/Dev)
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    val dump = buildString {
                        appendLine("val presets = listOf(")
                        appendLine("    \"Custom\" to listOf(")
                        workingWidgets.forEach { widget ->
                            append("        ")
                            when (widget) {
                                is CanvasWidget.RakatCount -> {
                                    append("CanvasWidget.RakatCount(")
                                    append("offsetX = ${widget.offsetX}f, offsetY = ${widget.offsetY}f, ")
                                    append("scale = ${widget.scale}f, color = ${widget.color}, ")
                                    append("opacity = ${widget.opacity}f, fontSizeSp = ${widget.fontSizeSp}f, ")
                                    append("fontWeight = ${widget.fontWeight}, isOutline = ${widget.isOutline}, ")
                                    append("fontName = \"${widget.fontName}\"")
                                    append(")")
                                }

                                is CanvasWidget.ClockWidget -> {
                                    append("CanvasWidget.ClockWidget(")
                                    append("offsetX = ${widget.offsetX}f, offsetY = ${widget.offsetY}f, ")
                                    append("scale = ${widget.scale}f, color = ${widget.color}, ")
                                    append("opacity = ${widget.opacity}f, fontSizeSp = ${widget.fontSizeSp}f, ")
                                    append("showSeconds = ${widget.showSeconds}, use24Hour = ${widget.use24Hour}, ")
                                    append("isOutline = ${widget.isOutline}, ")
                                    append("fontName = \"${widget.fontName}\"")
                                    append(")")
                                }

                                is CanvasWidget.CustomText -> {
                                    append("CanvasWidget.CustomText(")
                                    append("offsetX = ${widget.offsetX}f, offsetY = ${widget.offsetY}f, ")
                                    append("scale = ${widget.scale}f, text = \"${widget.text}\", color = ${widget.color}, ")
                                    append("opacity = ${widget.opacity}f, fontSizeSp = ${widget.fontSizeSp}f, ")
                                    append("fontWeight = ${widget.fontWeight}, italic = ${widget.italic}, ")
                                    append("textAlign = \"${widget.textAlign}\", verticalAlign = \"${widget.verticalAlign}\", ")
                                    append("isOutline = ${widget.isOutline}, ")
                                    append("fontName = \"${widget.fontName}\"")
                                    append(")")
                                }
                            }
                            appendLine(",")
                        }
                        appendLine("    ),")
                        appendLine(")")
                    }
                    Log.d("PRESET_DUMP", dump)
                }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Dev Dump",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
                TextButton(
                    onClick = onExit,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                ) {
                    Text("Exit")
                }
//                Button(
//                    onClick = { viewModel.saveLayout(workingBackground); onSave() },
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Text("Save")
//                }
                Button(
                    onClick = {
                        viewModel.saveLayout(workingBackground)
                        android.widget.Toast.makeText(context, "Salah Screen Saved", android.widget.Toast.LENGTH_SHORT).show()
                        // Notice we removed onSave() so it doesn't exit the screen anymore
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }

        // FABs - Add Widget and Presets
        var showAddMenu by remember { mutableStateOf(false) }
        var showPresetsMenu by remember { mutableStateOf(false) }

        AnimatedVisibility(
            visible = isUiVisible && selectedWidgetId == null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 30.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Presets FAB (left)
                FloatingActionButton(
                    onClick = { showPresetsMenu = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Presets")
                }

                // Add Widget FAB (right)
                ExtendedFloatingActionButton(
                    onClick = { showAddMenu = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Widget") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        if (showAddMenu) {
            AddWidgetSheet(
                currentBackground = workingBackground,
                onAdd = { widget ->
                    viewModel.addWidget(widget)
                    showAddMenu = false
                },
                onBackgroundChange = {
                    viewModel.updateBackgroundColor(it)
                },
                onDismiss = { showAddMenu = false }
            )
        }

        if (showPresetsMenu) {
            PresetsSheet(
                viewModel = viewModel,
                actualScreenWidth = screenWidth,
                actualScreenHeight = screenHeight,
                onDismiss = { showPresetsMenu = false },
                onLoadPreset = { widgets, bgColor ->
                    viewModel.clearWidgets()
                    widgets.forEach { viewModel.addWidget(it) }
                    viewModel.updateBackgroundColor(bgColor)
                    showPresetsMenu = false
                }
            )
        }

        // Selection / Config
        selectedWidgetId?.let { id ->
            workingWidgets.find { it.id == id }?.let { selectedWidget ->
                WidgetConfigSheet(
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
fun CanvasWidgetItem(
    widget: CanvasWidget,
    isSelected: Boolean,
    screenWidth: Float,
    screenHeight: Float,
    currentRakats: Int,
    onUpdate: (CanvasWidget) -> Unit,
    onTap: () -> Unit,
    onSizeMeasured: (String, Float, Float) -> Unit,
) {
    val currentWidget by rememberUpdatedState(widget)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnSizeMeasured by rememberUpdatedState(onSizeMeasured)

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = widget.offsetX
                translationY = widget.offsetY
                scaleX = widget.scale
                scaleY = widget.scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .then(
                if (isSelected) Modifier.border(
                    1.dp,
                    Color.White.copy(alpha = 0.4f),
                    RoundedCornerShape(4.dp)
                )
                else Modifier
            )
            .pointerInput(widget.id) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(widget.id) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, _ ->
                        val w = currentWidget
//                        val newOffsetX = (w.offsetX + pan.x).coerceAtLeast(0f)
//                        val newOffsetY = (w.offsetY + pan.y).coerceAtLeast(0f)
                        val newOffsetX = w.offsetX + pan.x
                        val newOffsetY = w.offsetY + pan.y
                        val newScale = (w.scale * zoom).coerceIn(0.2f, 5f)

                        val updated = when (w) {
                            is CanvasWidget.RakatCount -> w.copy(
                                offsetX = newOffsetX,
                                offsetY = newOffsetY,
                                scale = newScale
                            )

                            is CanvasWidget.ClockWidget -> w.copy(
                                offsetX = newOffsetX,
                                offsetY = newOffsetY,
                                scale = newScale
                            )

                            is CanvasWidget.CustomText -> w.copy(
                                offsetX = newOffsetX,
                                offsetY = newOffsetY,
                                scale = newScale
                            )
                        }
                        currentOnUpdate(updated)
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                currentOnSizeMeasured(
                    widget.id,
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            }
    ) {
        // Render widgets directly without remember block to avoid recomposition lag
        val widgetFontFamily = when (widget) {
            is CanvasWidget.RakatCount -> widget.fontName.resolveFontFamily()
            is CanvasWidget.ClockWidget -> widget.fontName.resolveFontFamily()
            is CanvasWidget.CustomText -> widget.fontName.resolveFontFamily()
        }
        when (widget) {
            is CanvasWidget.RakatCount -> {
                Text(
                    text = currentRakats.toString(),
                    fontFamily = widgetFontFamily,
                    fontSize = widget.fontSizeSp.sp,
                    fontWeight = FontWeight(widget.fontWeight),
                    style = TextStyle(
                        color = Color(widget.color).copy(alpha = widget.opacity),
                        drawStyle = if (widget.isOutline) Stroke(
                            width = 4f,
                            join = StrokeJoin.Round
                        ) else Fill,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }

            is CanvasWidget.ClockWidget -> {
                LiveClockText(widget, widget.opacity, widget.isOutline)
            }

            is CanvasWidget.CustomText -> {
                Box(
                    contentAlignment = when (widget.verticalAlign) {
                        "Top" -> Alignment.TopCenter
                        "Bottom" -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
                ) {
                    Text(
                        text = widget.text,
                        fontFamily = widgetFontFamily,
                        fontSize = widget.fontSizeSp.sp,
                        fontWeight = FontWeight(widget.fontWeight),
                        fontStyle = if (widget.italic) FontStyle.Italic else FontStyle.Normal,
                        textAlign = when (widget.textAlign) {
                            "Left" -> TextAlign.Left
                            "Right" -> TextAlign.Right
                            else -> TextAlign.Center
                        },
                        style = TextStyle(
                            color = Color(widget.color).copy(alpha = widget.opacity),
                            drawStyle = if (widget.isOutline) Stroke(
                                width = 4f,
                                join = StrokeJoin.Round
                            ) else Fill,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveClockText(widget: CanvasWidget.ClockWidget, opacity: Float, isOutline: Boolean) {
    var timeStr by remember { mutableStateOf("") }
    LaunchedEffect(widget.showSeconds, widget.use24Hour) {
        while (true) {
            val now = java.time.LocalTime.now()
            val h = if (widget.use24Hour) now.hour else now.hour.let { if (it == 0) 12 else it }
            val m = now.minute
            val s = now.second
            timeStr = if (widget.showSeconds) "%02d:%02d:%02d".format(h, m, s)
            else "%02d:%02d".format(h, m)
            delay(1000)
        }
    }
    val fontFamily = widget.fontName.resolveFontFamily()
    Text(
        text = timeStr,
        fontFamily = fontFamily,
        fontSize = widget.fontSizeSp.sp,
        style = TextStyle(
            color = Color(widget.color).copy(alpha = opacity),
            drawStyle = if (isOutline) Stroke(width = 4f, join = StrokeJoin.Round) else Fill,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
}

@Composable
private fun LiveClockTextPreview(widget: CanvasWidget.ClockWidget) {
    val fontFamily = widget.fontName.resolveFontFamily()
    Text(
        text = if (widget.showSeconds) "12:00:00" else "12:00",
        fontFamily = fontFamily,
        fontSize = widget.fontSizeSp.sp,
        style = TextStyle(
            color = Color(widget.color).copy(alpha = widget.opacity),
            drawStyle = if (widget.isOutline) Stroke(width = 4f, join = StrokeJoin.Round) else Fill,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
}

private val CANVAS_BACKGROUNDS = listOf(
    Pair(Color(0xFF000000), "True Black"),        // True black for OLED
    Pair(Color(0xFF121212), "Soft Black"),       // Soft black 
    Pair(Color(0xFF16213E), "Midnight Blue"),    // Dark Blue
    Pair(Color(0xFF1B4332), "Dark Green"),       // Dark Green
    Pair(Color(0xFF2C3E50), "Charcoal"),         // Dark Slate
)

private val WIDGET_COLORS = listOf(
    Color.White,
    Color(0xFFD4AF37), // Gold
    Color(0xFF90EE90), // Light Green
    Color(0xFF87CEEB), // Sky Blue
    Color(0xFFFFB6C1), // Light Pink
    Color(0xFFE6E6FA), // Lavender
    Color(0xFFFFD700), // Bright Gold
    Color(0xFF98FB98), // Pale Green
    Color(0xFFADD8E6), // Light Blue
    Color(0xFFFFB347), // Pastel Orange
    Color(0xFFFF6961), // Pastel Red
    Color(0xFFB19CD9), // Pastel Purple
    Color(0xFF779ECB), // Steel Blue
    Color(0xFFAEC6CF), // Pastel Blue
    Color(0xFFFFDAB9), // Peach
    Color(0xFF98D8C8), // Mint
)

private fun Color.toIntArgb(): Int {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

private fun Int.toColor(): Color = Color(this.toLong())

private fun Color.toSolidInt(): Int {
    return 0xFF000000.toInt() or toIntArgb()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWidgetSheet(
    currentBackground: Int,
    onAdd: (CanvasWidget) -> Unit,
    onBackgroundChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "CANVAS BACKGROUND",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // Premium Color Picker with Scale Pop & Checkmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CANVAS_BACKGROUNDS.take(5).forEach { (color, _) ->
                    val colorInt = color.toSolidInt()
                    val isSelected = currentBackground == colorInt

                    Box(
                        modifier = Modifier
                            // Physical scale pop for selected item
                            .size(if (isSelected) 52.dp else 44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { onBackgroundChange(colorInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        // High-contrast checkmark overlay
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Custom Color Gradient Wheel
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Red,
                                    Color.Magenta,
                                    Color.Blue,
                                    Color.Cyan,
                                    Color.Green,
                                    Color.Yellow,
                                    Color.Red
                                )
                            )
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { showColorPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Custom Color",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showColorPicker) {
                Spacer(Modifier.height(24.dp))
                ColorPicker(
                    onColorSelected = { colorInt ->
                        onBackgroundChange(colorInt)
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
                )
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))

            Text(
                text = "ADD WIDGET",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // Visual Grid Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rakat Preview Card
                WidgetPreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Rakat Counter",
                    onClick = { onAdd(CanvasWidget.RakatCount()) }
                ) {
                    Text(
                        text = "4",
                        fontFamily = Antonio,
                        fontSize = 56.sp,
                        color = Color.White
                    )
                }

                // Clock Preview Card
                WidgetPreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Digital Clock",
                    onClick = { onAdd(CanvasWidget.ClockWidget()) }
                ) {
                    Text(
                        text = "12:00",
                        fontFamily = BeVietnamPro,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Custom Text Full-Width Banner
            WidgetPreviewCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Custom Text",
                onClick = { onAdd(CanvasWidget.CustomText()) }
            ) {
                Text(
                    text = "صلاة",
                    fontFamily = BeVietnamPro,
                    fontSize = 36.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White
                )
            }
        }
    }
}

// ─── New Reusable Preview Card ────────────────────────────────────────────────

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
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Miniature Canvas Representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)), // Deep black to simulate the canvas
                contentAlignment = Alignment.Center,
                content = previewContent
            )

            // Label Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetsSheet(
    viewModel: SalahCanvasViewModel,
    actualScreenWidth: Float,
    actualScreenHeight: Float,
    onDismiss: () -> Unit,
    onLoadPreset: (List<CanvasWidget>, Int) -> Unit,
) {
    val workingWidgets by viewModel.workingWidgets.collectAsStateWithLifecycle()
    val workingBackground by viewModel.workingBackgroundColor.collectAsStateWithLifecycle()

    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()

    var actionPreset by remember { mutableStateOf<CanvasPreset?>(null) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showInterceptedSaveDialog by remember { mutableStateOf(false) }

    val currentPreset = CanvasPreset(id = "current", name = "Current", backgroundColor = workingBackground, widgets = workingWidgets, isDeletable = false)
    val presets = listOf(currentPreset) + customPresets + DefaultPresets.defaults

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = BeVietnamPro,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        val clearAction = {
                            viewModel.clearWidgets()
                            viewModel.updateBackgroundColor(0xFF000000.toInt())
                            onDismiss()
                        }

                        if (workingWidgets.isNotEmpty()) {
                            pendingAction = clearAction
                            showUnsavedDialog = true
                        } else {
                            clearAction()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                ) {
                    Text(
                        text = "Create",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = BeVietnamPro,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp.dp
            val horizontalPadding = (screenWidthDp - 230.dp) / 2

            val pagerState = rememberPagerState(pageCount = { presets.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                val currentPreset = presets[pagerState.currentPage]
                val dominantColor = currentPreset.widgets.firstOrNull()?.let {
                    when (it) {
                        is CanvasWidget.RakatCount -> Color(it.color)
                        is CanvasWidget.ClockWidget -> Color(it.color)
                        is CanvasWidget.CustomText -> Color(it.color)
                    }
                } ?: MaterialTheme.colorScheme.surfaceVariant

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .blur(radius = 80.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    dominantColor.copy(alpha = 0.6f),
                                    dominantColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                radius = 400f
                            )
                        )
                )

                HorizontalPager(
                    state = pagerState,
                    key = { index -> presets[index].id },
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val preset = presets[page]
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                    val cardScale = 1f - (kotlin.math.abs(pageOffset) * 0.15f).coerceIn(0f, 0.15f)
                    val cardAlpha = 1f - (kotlin.math.abs(pageOffset) * 0.5f).coerceIn(0f, 0.5f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                        }
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .width(230.dp)
                                .height(440.dp)
                        ) {
                            val previewScale = constraints.maxWidth.toFloat() / actualScreenWidth

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color(preset.backgroundColor))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .pointerInput(preset.id) {
                                        detectTapGestures(
                                            onTap = {
                                                if (preset.id == "current") return@detectTapGestures

                                                val loadAction = {
                                                    viewModel.clearWidgets()
                                                    preset.widgets.forEach { viewModel.addWidget(it) }
                                                    viewModel.updateBackgroundColor(preset.backgroundColor)
                                                    onDismiss()
                                                }

                                                if (workingWidgets.isNotEmpty()) {
                                                    pendingAction = loadAction
                                                    showUnsavedDialog = true
                                                } else {
                                                    loadAction()
                                                }
                                            },
                                            onLongPress = {
                                                if (preset.isDeletable) {
                                                    // This triggers the action menu below
                                                    actionPreset = preset
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.TopStart
                            ) {
                                // Your existing preview content logic
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = previewScale
                                            scaleY = previewScale
                                            transformOrigin = TransformOrigin(0f, 0f)
                                        }
                                ) {
                                    preset.widgets.forEach { widget ->
                                        // Keeping your exact rendering loop here to avoid breaking it
                                        val widgetFontFamily = when (widget) {
                                            is CanvasWidget.RakatCount -> widget.fontName.resolveFontFamily()
                                            is CanvasWidget.ClockWidget -> widget.fontName.resolveFontFamily()
                                            is CanvasWidget.CustomText -> widget.fontName.resolveFontFamily()
                                        }
                                        Box(
                                            modifier = Modifier.graphicsLayer {
                                                translationX = widget.offsetX
                                                translationY = widget.offsetY
                                                scaleX = widget.scale
                                                scaleY = widget.scale
                                                transformOrigin = TransformOrigin(0f, 0f)
                                            }
                                        ) {
                                            when (widget) {
                                                is CanvasWidget.RakatCount -> {
                                                    Text(
                                                        text = "4",
                                                        fontFamily = widgetFontFamily,
                                                        fontSize = widget.fontSizeSp.sp,
                                                        fontWeight = FontWeight(widget.fontWeight),
                                                        style = TextStyle(
                                                            color = Color(widget.color).copy(alpha = widget.opacity),
                                                            drawStyle = if (widget.isOutline) Stroke(
                                                                width = 4f,
                                                                join = StrokeJoin.Round
                                                            ) else Fill,
                                                            platformStyle = PlatformTextStyle(
                                                                includeFontPadding = false
                                                            )
                                                        )
                                                    )
                                                }

                                                is CanvasWidget.ClockWidget -> {
                                                    LiveClockTextPreview(widget)
                                                }

                                                is CanvasWidget.CustomText -> {
                                                    Box(
                                                        contentAlignment = when (widget.verticalAlign) {
                                                            "Top" -> Alignment.TopCenter; "Bottom" -> Alignment.BottomCenter; else -> Alignment.Center
                                                        }
                                                    ) {
                                                        Text(
                                                            text = widget.text ?: "",
                                                            fontFamily = widgetFontFamily,
                                                            fontSize = widget.fontSizeSp.sp,
                                                            fontWeight = FontWeight(widget.fontWeight),
                                                            fontStyle = if (widget.italic) FontStyle.Italic else FontStyle.Normal,
                                                            textAlign = when (widget.textAlign) {
                                                                "Left" -> TextAlign.Left; "Right" -> TextAlign.Right; else -> TextAlign.Center
                                                            },
                                                            style = TextStyle(
                                                                color = Color(widget.color).copy(
                                                                    alpha = widget.opacity
                                                                ),
                                                                drawStyle = if (widget.isOutline) Stroke(
                                                                    width = 4f,
                                                                    join = StrokeJoin.Round
                                                                ) else Fill,
                                                                platformStyle = PlatformTextStyle(
                                                                    includeFontPadding = false
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = BeVietnamPro,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(presets.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.3f
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }

    // ─── ACTION MENU (Appears when a preset is long-pressed) ───
    actionPreset?.let { targetPreset ->
        var showRenameDialog by remember { mutableStateOf(false) }

        if (showRenameDialog) {
            SavePresetDialog(
                initialName = targetPreset.name,
                onDismiss = {
                    showRenameDialog = false
                    actionPreset = null
                },
                onConfirm = { newName ->
                    viewModel.renamePreset(targetPreset.id, newName)
                    showRenameDialog = false
                    actionPreset = null
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { actionPreset = null },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = targetPreset.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    ListItem(
                        headlineContent = { Text("Rename Preset") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable { showRenameDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                "Delete Preset",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable {
                            viewModel.deletePreset(targetPreset.id)
                            actionPreset = null
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
    // ─── INTERCEPTOR DIALOGS ───
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = {
                showUnsavedDialog = false
                pendingAction = null
            },
            onDiscard = {
                showUnsavedDialog = false
                pendingAction?.invoke() // Executes the load/clear immediately
                pendingAction = null
            },
            onSave = {
                showUnsavedDialog = false
                showInterceptedSaveDialog = true // Push them to the naming dialog
            }
        )
    }

    if (showInterceptedSaveDialog) {
        SavePresetDialog(
            initialName = "",
            onDismiss = {
                showInterceptedSaveDialog = false
                pendingAction = null // Abort everything if they cancel naming
            },
            onConfirm = { newName ->
                viewModel.saveCustomPreset(newName, workingWidgets, workingBackground)
                showInterceptedSaveDialog = false
                pendingAction?.invoke() // Executes the load/clear after saving
                pendingAction = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigSheet(
    widget: CanvasWidget,
    onUpdate: (CanvasWidget) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onAlign: (Alignment.Horizontal?, Alignment.Vertical?) -> Unit,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface, // Fixes invisible text bug
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) // Locks to 75% screen height
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── HEADER ───────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (widget) {
                        is CanvasWidget.RakatCount -> "Configure Counter"
                        is CanvasWidget.ClockWidget -> "Configure Clock"
                        is CanvasWidget.CustomText -> "Configure Text"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Widget")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── TEXT CONTENT (If applicable, moved to top for UX) ────
            if (widget is CanvasWidget.CustomText) {
                OutlinedTextField(
                    value = widget.text ?: "",
                    onValueChange = { onUpdate(widget.copy(text = it)) },
                    label = { Text("Text Content") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── COLOR PICKER (5 + 1) ─────────────────────────────────
            Text(
                "COLOR",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            val widgetColor = when (widget) {
                is CanvasWidget.RakatCount -> widget.color
                is CanvasWidget.ClockWidget -> widget.color
                is CanvasWidget.CustomText -> widget.color
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WIDGET_COLORS.take(5).forEach { color ->
                    val colorInt = color.toIntArgb()
                    val isSelected = widgetColor == colorInt

                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 52.dp else 44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                val updated = when (widget) {
                                    is CanvasWidget.RakatCount -> widget.copy(color = colorInt)
                                    is CanvasWidget.ClockWidget -> widget.copy(color = colorInt)
                                    is CanvasWidget.CustomText -> widget.copy(color = colorInt)
                                }
                                onUpdate(updated)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Custom Color Wheel
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.sweepGradient(
                                listOf(
                                    Color.Red,
                                    Color.Magenta,
                                    Color.Blue,
                                    Color.Cyan,
                                    Color.Green,
                                    Color.Yellow,
                                    Color.Red
                                )
                            )
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { showColorPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Custom",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showColorPicker) {
                Spacer(Modifier.height(16.dp))
                ColorPicker(
                    onColorSelected = { colorInt ->
                        val updated = when (widget) {
                            is CanvasWidget.RakatCount -> widget.copy(color = colorInt)
                            is CanvasWidget.ClockWidget -> widget.copy(color = colorInt)
                            is CanvasWidget.CustomText -> widget.copy(color = colorInt)
                        }
                        onUpdate(updated)
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))

            // ── FORMATTING & STYLE BAR ───────────────────────────────
            Text(
                "STYLE & FORMAT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // Compact Toggle Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Outline Toggle (Available to all)
                val isOutline = when (widget) {
                    is CanvasWidget.RakatCount -> widget.isOutline
                    is CanvasWidget.ClockWidget -> widget.isOutline
                    is CanvasWidget.CustomText -> widget.isOutline
                }
                FilterChip(
                    selected = isOutline,
                    onClick = {
                        val updated = when (widget) {
                            is CanvasWidget.RakatCount -> widget.copy(isOutline = !isOutline)
                            is CanvasWidget.ClockWidget -> widget.copy(isOutline = !isOutline)
                            is CanvasWidget.CustomText -> widget.copy(isOutline = !isOutline)
                        }
                        onUpdate(updated)
                    },
                    label = { Text("Outline") }
                )

                // Bold Toggle
                if (widget is CanvasWidget.RakatCount || widget is CanvasWidget.CustomText) {
                    val isBold = when (widget) {
                        is CanvasWidget.RakatCount -> widget.fontWeight == 700
                        is CanvasWidget.CustomText -> widget.fontWeight == 700
                        else -> false
                    }
                    FilterChip(
                        selected = isBold,
                        onClick = {
                            val newWeight = if (isBold) 400 else 700
                            val updated = when (widget) {
                                is CanvasWidget.RakatCount -> widget.copy(fontWeight = newWeight)
                                is CanvasWidget.CustomText -> widget.copy(fontWeight = newWeight)
                                else -> widget
                            }
                            onUpdate(updated)
                        },
                        label = { Text("Bold") }
                    )
                }

                // Italic Toggle
                if (widget is CanvasWidget.CustomText) {
                    FilterChip(
                        selected = widget.italic,
                        onClick = { onUpdate(widget.copy(italic = !widget.italic)) },
                        label = { Text("Italic") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── SLIDERS (Size & Opacity) ─────────────────────────────
            val widgetSize = when (widget) {
                is CanvasWidget.RakatCount -> widget.fontSizeSp
                is CanvasWidget.ClockWidget -> widget.fontSizeSp
                is CanvasWidget.CustomText -> widget.fontSizeSp
            }
            SliderControl("SIZE", widgetSize, 12f, 300f) { newValue ->
                val updated = when (widget) {
                    is CanvasWidget.RakatCount -> widget.copy(fontSizeSp = newValue)
                    is CanvasWidget.ClockWidget -> widget.copy(fontSizeSp = newValue)
                    is CanvasWidget.CustomText -> widget.copy(fontSizeSp = newValue)
                }
                onUpdate(updated)
            }

            Spacer(Modifier.height(16.dp))

            val widgetOpacity = when (widget) {
                is CanvasWidget.RakatCount -> widget.opacity
                is CanvasWidget.ClockWidget -> widget.opacity
                is CanvasWidget.CustomText -> widget.opacity
            }
            SliderControl(
                "OPACITY (${(widgetOpacity * 100).toInt()}%)",
                widgetOpacity,
                0.1f,
                1f
            ) { newValue ->
                val updated = when (widget) {
                    is CanvasWidget.RakatCount -> widget.copy(opacity = newValue)
                    is CanvasWidget.ClockWidget -> widget.copy(opacity = newValue)
                    is CanvasWidget.CustomText -> widget.copy(opacity = newValue)
                }
                onUpdate(updated)
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))

            // ── ALIGNMENT ────────────────────────────────────────────
            Text(
                "ALIGNMENT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // Horizontal Alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Alignment.Start to Icons.AutoMirrored.Filled.AlignHorizontalLeft,
                    Alignment.CenterHorizontally to Icons.Default.AlignHorizontalCenter,
                    Alignment.End to Icons.AutoMirrored.Filled.AlignHorizontalRight
                ).forEach { (align, icon) ->
                    FilledTonalButton(
                        onClick = { onAlign(align, null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Vertical Alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Alignment.Top to Icons.Default.VerticalAlignTop,
                    Alignment.CenterVertically to Icons.Default.VerticalAlignCenter,
                    Alignment.Bottom to Icons.Default.VerticalAlignBottom
                ).forEach { (align, icon) ->
                    FilledTonalButton(
                        onClick = { onAlign(null, align) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── CLOCK SPECIFICS (12H / 24H) ──────────────────────────
            if (widget is CanvasWidget.ClockWidget) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(24.dp))
                Text(
                    "TIME FORMAT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !widget.use24Hour,
                        onClick = { onUpdate(widget.copy(use24Hour = false)) },
                        label = { Text("12-Hour") }
                    )
                    FilterChip(
                        selected = widget.use24Hour,
                        onClick = { onUpdate(widget.copy(use24Hour = true)) },
                        label = { Text("24-Hour") }
                    )
                    FilterChip(
                        selected = widget.showSeconds,
                        onClick = { onUpdate(widget.copy(showSeconds = !widget.showSeconds)) },
                        label = { Text("Show Seconds") }
                    )
                }
            }
        }
    }
}

// ── Refactored Slider Helper ──
@Composable
private fun SliderControl(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onUpdate: (Float) -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onUpdate,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ColorPicker(
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var red by remember { mutableFloatStateOf(0.1f) }
    var green by remember { mutableFloatStateOf(0.1f) }
    var blue by remember { mutableFloatStateOf(0.1f) }

    val selectedColor = Color(red, green, blue)
    val selectedColorInt = selectedColor.toSolidInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            "Custom Background Color",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(16.dp))

        // Color preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(selectedColor)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(16.dp))

        // Red slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
            Spacer(Modifier.width(8.dp))
            Slider(
                value = red,
                onValueChange = { red = it },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text("${(red * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
        }

        // Green slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
            )
            Spacer(Modifier.width(8.dp))
            Slider(
                value = green,
                onValueChange = { green = it },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text("${(green * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
        }

        // Blue slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Blue)
            )
            Spacer(Modifier.width(8.dp))
            Slider(
                value = blue,
                onValueChange = { blue = it },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text("${(blue * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onColorSelected(selectedColorInt) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var presetName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "Save Preset" else "Rename Preset") },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text("Preset Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(presetName) },
                enabled = presetName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Current Canvas?") },
        text = { Text("You are about to load a new layout. Do you want to save your current canvas as a preset first?") },
        confirmButton = {
            Button(onClick = onSave) { Text("Save as Preset") }
        },
        dismissButton = {
            TextButton(onClick = onDiscard, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Discard")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}