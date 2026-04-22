package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.*
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasbihBeadCustomizerSheet(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
) {
    val settings by settingsViewModel.settings.collectAsState()

    var workingStyle by remember {
        val current = settings.customBeadStyles.find { it.id == settings.activeBeadStyleId }
        mutableStateOf(current ?: CustomBeadStyle(id = "default", name = "My Design"))
    }

    val circle = MaterialShapes.Circle.toShape()
    val square = MaterialShapes.Square.toShape()
    val triangle = MaterialShapes.Triangle.toShape()
    val diamond = MaterialShapes.Diamond.toShape()
    val pill = MaterialShapes.Pill.toShape()
    val slanted = MaterialShapes.Slanted.toShape()
    val semiCircle = MaterialShapes.SemiCircle.toShape()
    val arch = MaterialShapes.Arch.toShape()
    val cookie4 = MaterialShapes.Cookie4Sided.toShape()
    val cookie6 = MaterialShapes.Cookie6Sided.toShape()
    val cookie7 = MaterialShapes.Cookie7Sided.toShape()
    val cookie9 = MaterialShapes.Cookie9Sided.toShape()
    val cookie12 = MaterialShapes.Cookie12Sided.toShape()
    val clover4 = MaterialShapes.Clover4Leaf.toShape()
    val clover8 = MaterialShapes.Clover8Leaf.toShape()
    val puffy = MaterialShapes.Puffy.toShape()
    val puffyDiamond = MaterialShapes.PuffyDiamond.toShape()
    val sunny = MaterialShapes.Sunny.toShape()
    val verySunny = MaterialShapes.VerySunny.toShape()
    val burst = MaterialShapes.Burst.toShape()
    val softBurst = MaterialShapes.SoftBurst.toShape()
    val boom = MaterialShapes.Boom.toShape()
    val softBoom = MaterialShapes.SoftBoom.toShape()
    val heart = MaterialShapes.Heart.toShape()
    val gem = MaterialShapes.Gem.toShape()
    val bun = MaterialShapes.Bun.toShape()
    val clamshell = MaterialShapes.ClamShell.toShape()
    val fan = MaterialShapes.Fan.toShape()
    val arrow = MaterialShapes.Arrow.toShape()
    val pixelCircle = MaterialShapes.PixelCircle.toShape()
    val pixelTriangle = MaterialShapes.PixelTriangle.toShape()

    val allShapes = remember {
        mapOf(
            BeadShapeType.CIRCLE to circle,
            BeadShapeType.SQUARE to square,
            BeadShapeType.TRIANGLE to triangle,
            BeadShapeType.DIAMOND to diamond,
            BeadShapeType.PILL to pill,
            BeadShapeType.SLANTED to slanted,
            BeadShapeType.SEMI_CIRCLE to semiCircle,
            BeadShapeType.ARCH to arch,
            BeadShapeType.COOKIE_4 to cookie4,
            BeadShapeType.COOKIE_6 to cookie6,
            BeadShapeType.COOKIE_7 to cookie7,
            BeadShapeType.COOKIE_9 to cookie9,
            BeadShapeType.COOKIE_12 to cookie12,
            BeadShapeType.CLOVER_4 to clover4,
            BeadShapeType.CLOVER_8 to clover8,
            BeadShapeType.PUFFY to puffy,
            BeadShapeType.PUFFY_DIAMOND to puffyDiamond,
            BeadShapeType.SUNNY to sunny,
            BeadShapeType.VERY_SUNNY to verySunny,
            BeadShapeType.BURST to burst,
            BeadShapeType.SOFT_BURST to softBurst,
            BeadShapeType.BOOM to boom,
            BeadShapeType.SOFT_BOOM to softBoom,
            BeadShapeType.HEART to heart,
            BeadShapeType.GEM to gem,
            BeadShapeType.BUN to bun,
            BeadShapeType.CLAMSHELL to clamshell,
            BeadShapeType.FAN to fan,
            BeadShapeType.ARROW to arrow,
            BeadShapeType.PIXEL_CIRCLE to pixelCircle,
            BeadShapeType.PIXEL_TRIANGLE to pixelTriangle,
        )
    }

    var showColorPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Design Your Bead",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    val beadShape = allShapes[workingStyle.shapeType] ?: CircleShape
                    BeadPreview(
                        style = workingStyle,
                        beadShape = beadShape,
                        onUpdate = { workingStyle = it }
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionHeader("Quick Presets")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(BeadPreset.entries.filter { it != BeadPreset.NONE }, key = { it.name }) { preset ->
                        val previewStyle = remember(preset, workingStyle.shapeType) {
                            previewStyleForPreset(preset, workingStyle.shapeType)
                        }
                        val thumbShape = allShapes[workingStyle.shapeType] ?: CircleShape
                        val isSelected = workingStyle.preset == preset
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { workingStyle = workingStyle.applyPreset(preset) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BeadRenderer(style = previewStyle, shape = thumbShape, size = 52f)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                SectionHeader("Saved Designs")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        OutlinedCard(
                            onClick = { workingStyle = CustomBeadStyle(id = UUID.randomUUID().toString(), name = "New Design") },
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, null)
                            }
                        }
                    }
                    items(settings.customBeadStyles, key = { it.id }) { style ->
                        val isSelected = workingStyle.id == style.id
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable { workingStyle = style }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val thumbShape = allShapes[style.shapeType] ?: CircleShape
                            BeadRenderer(style = style, shape = thumbShape, size = 44f)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                SectionHeader("Bead Shape")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BeadShapeType.entries, key = { it.name }) { shapeType ->
                        val isSelected = workingStyle.shapeType == shapeType
                        val m3Shape = allShapes[shapeType] ?: CircleShape
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable { workingStyle = workingStyle.copy(shapeType = shapeType) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        shape = m3Shape
                                        clip = true
                                    }
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                SectionHeader("Base Color")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)))
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { showColorPicker = true }
                        )
                    }

                    val premiumColors = listOf(
                        0xFFD4850A, 0xFF7A4000, 0xFF1A1A1A, 0xFF1B4332,
                        0xFF432818, 0xFF0D1B2A, 0xFF2C3E50, 0xFF5D4037,
                        0xFF34495E, 0xFF212121
                    )
                    items(premiumColors, key = { it }) { colorInt ->
                        val isSelected = workingStyle.baseColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { workingStyle = workingStyle.copy(baseColor = colorInt) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                SectionHeader("Effects")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = workingStyle.chromaticAberration,
                        onClick = {
                            workingStyle = workingStyle.copy(
                                chromaticAberration = !workingStyle.chromaticAberration,
                                preset = BeadPreset.NONE
                            )
                        },
                        label = { Text("Chromatic", fontFamily = BeVietnamPro) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = workingStyle.metallicSheen,
                        onClick = {
                            workingStyle = workingStyle.copy(
                                metallicSheen = !workingStyle.metallicSheen,
                                preset = BeadPreset.NONE
                            )
                        },
                        label = { Text("Metallic", fontFamily = BeVietnamPro) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = workingStyle.is3dEnabled,
                        onClick = {
                            workingStyle = workingStyle.copy(
                                is3dEnabled = !workingStyle.is3dEnabled,
                                preset = BeadPreset.NONE
                            )
                        },
                        label = { Text("3D Depth", fontFamily = BeVietnamPro) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Light",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                    Slider(
                        value = workingStyle.specularity,
                        onValueChange = {
                            workingStyle = workingStyle.copy(specularity = it, preset = BeadPreset.NONE)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionHeader("Engraving")
                OutlinedTextField(
                    value = workingStyle.engravingText,
                    onValueChange = { if (it.length <= 10) workingStyle = workingStyle.copy(engravingText = it) },
                    label = { Text("Custom Text") },
                    placeholder = { Text("e.g. الله") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Text(
                    text = "Pinch to scale  ·  Drag to move  ·  Double tap to reset",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = {
                        settingsViewModel.saveCustomBeadStyle(workingStyle)
                        settingsViewModel.setActiveBeadStyleId(workingStyle.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save & Apply Style", fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
                }

                if (workingStyle.id != "default" && settings.customBeadStyles.any { it.id == workingStyle.id }) {
                    TextButton(
                        onClick = {
                            settingsViewModel.deleteCustomBeadStyle(workingStyle.id)
                            workingStyle = settings.customBeadStyles.firstOrNull() ?: CustomBeadStyle(id = "default", name = "Default")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Design")
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showColorPicker) {
        SimpleColorPickerSheet(
            initialColor = Color(workingStyle.baseColor),
            onColorSelected = { workingStyle = workingStyle.copy(baseColor = it.toArgb().toLong()) },
            onDismiss = { showColorPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleColorPickerSheet(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    var hexString by remember { mutableStateOf("") }

    // Sync HSV from initial color
    LaunchedEffect(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        hexString = String.format("#%06X", 0xFFFFFF and initialColor.toArgb())
    }

    val currentColor = Color.hsv(hue, saturation, value)

    // Sync Hex text when HSV sliders are moved
    LaunchedEffect(hue, saturation, value) {
        hexString = String.format("#%06X", 0xFFFFFF and currentColor.toArgb())
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text("Custom Color", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            Box(Modifier.size(100.dp).align(Alignment.CenterHorizontally).background(currentColor, RoundedCornerShape(16.dp)))

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = hexString,
                onValueChange = { newHex ->
                    hexString = newHex
                    if (newHex.length == 7 && newHex.startsWith("#")) {
                        try {
                            val parsedColor = Color(android.graphics.Color.parseColor(newHex))
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(parsedColor.toArgb(), hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            value = hsv[2]
                        } catch (e: Exception) {
                            // Ignore bad hex typing
                        }
                    }
                },
                label = { Text("HEX Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text("Hue: ${hue.toInt()}", style = MaterialTheme.typography.labelMedium)
            Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)

            Text("Saturation", style = MaterialTheme.typography.labelMedium)
            Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)

            Text("Brightness", style = MaterialTheme.typography.labelMedium)
            Slider(value = value, onValueChange = { value = it }, valueRange = 0f..1f)

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onColorSelected(currentColor); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Color")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

private fun previewStyleForPreset(preset: BeadPreset, shapeType: BeadShapeType): CustomBeadStyle =
    CustomBeadStyle(id = "preview_${preset.name}", name = "").applyPreset(preset).copy(shapeType = shapeType)

@Composable
private fun BeadPreview(
    style: CustomBeadStyle,
    beadShape: Shape,
    onUpdate: (CustomBeadStyle) -> Unit
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Keep gesture lambdas reading the latest style/callback without restarting the coroutine.
    val styleRef by rememberUpdatedState(style)
    val onUpdateRef by rememberUpdatedState(onUpdate)

    // Hit-testing region: Maps the exact mathematical curves of the shape
    val shapeRegion = remember(beadShape, density, layoutDirection) {
        val sizePx = with(density) { Size(100.dp.toPx(), 100.dp.toPx()) }
        val outline = beadShape.createOutline(sizePx, layoutDirection, density)
        val path = android.graphics.Path()

        when (outline) {
            is Outline.Rectangle -> path.addRect(outline.rect.left, outline.rect.top, outline.rect.right, outline.rect.bottom, android.graphics.Path.Direction.CW)
            is Outline.Rounded -> {
                val r = outline.roundRect
                path.addRoundRect(r.left, r.top, r.right, r.bottom, r.topLeftCornerRadius.x, r.topLeftCornerRadius.y, android.graphics.Path.Direction.CW)
            }
            is Outline.Generic -> path.set(outline.path.asAndroidPath())
        }

        val bounds = android.graphics.RectF()
        path.computeBounds(bounds, true)
        val region = android.graphics.Region()
        region.setPath(path, android.graphics.Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt()))
        region
    }

    // Check if the center of the text has left the shape
    val textCenterXPx = with(density) { 50.dp.toPx() + style.textOffsetX.dp.toPx() }
    val textCenterYPx = with(density) { 50.dp.toPx() + style.textOffsetY.dp.toPx() }
    val isInsideShape = shapeRegion.contains(textCenterXPx.toInt(), textCenterYPx.toInt())

    // The Unclipped Gesture Arena — single pointerInput to avoid competing nodes.
    // styleRef/onUpdateRef via rememberUpdatedState so the long-lived coroutine always
    // reads the latest values (fixes the stale-closure bug that made gestures feel
    // restricted to the original position/scale).
    Box(
        modifier = Modifier
            .size(100.dp)
            .pointerInput(Unit) {
                coroutineScope {
                    launch {
                        detectTapGestures(onDoubleTap = {
                            onUpdateRef(styleRef.copy(textScale = 1.0f, textOffsetX = 0f, textOffsetY = 0f))
                        })
                    }
                    launch {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val s = styleRef
                            val newScale = (s.textScale * zoom).coerceIn(0.5f, 5f)
                            val newOffsetX = s.textOffsetX + (pan.x / density.density)
                            val newOffsetY = s.textOffsetY + (pan.y / density.density)
                            onUpdateRef(s.copy(textScale = newScale, textOffsetX = newOffsetX, textOffsetY = newOffsetY))
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Canvas with full premium effects
        val noiseShader = remember { createNoiseShader(128) }
        val noiseBrush = remember(noiseShader) { ShaderBrush(noiseShader) }
        val previewSizePx = with(density) { 100.dp.toPx() }
        val previewPathSize = remember(previewSizePx) { Size(previewSizePx, previewSizePx) }
        val previewBeadPath = remember(beadShape, previewSizePx) {
            createBeadPath(beadShape, Size(previewSizePx, previewSizePx), layoutDirection, density)
        }
        val previewBrushCache = remember(styleRef, previewSizePx) {
            BeadBrushCache(
                noiseBrush = if (styleRef.textureStyle != BeadTextureStyle.SOLID) noiseBrush else null,
                specularBrush = buildSpecularBrush(styleRef, previewPathSize),
                metallicBrush = buildMetallicBrush(styleRef, previewPathSize),
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPremiumBead(previewBeadPath, styleRef, previewBrushCache)
        }

        // LAYER 2: The floating, GPU-accelerated text
        Text(
            text = style.engravingText,
            color = if (isInsideShape) Color.White.copy(alpha = 0.85f) else Color.Red.copy(alpha = 0.6f),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp, // CRITICAL FIX: Static font size stops the lag.
                fontFamily = Antonio,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                // CRITICAL FIX: graphicsLayer bypasses layout constraints, allowing free movement.
                .graphicsLayer {
                    translationX = style.textOffsetX * density.density
                    translationY = style.textOffsetY * density.density
                    scaleX = style.textScale
                    scaleY = style.textScale
                }
        )
    }
}

@Composable
private fun BeadRenderer(style: CustomBeadStyle, shape: Shape, size: Float) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val sizePx = with(density) { size.dp.toPx() }
    val noiseShader = remember { createNoiseShader(64) }
    val noiseBrush = remember(noiseShader) { ShaderBrush(noiseShader) }
    val pathSize = remember(sizePx) { Size(sizePx, sizePx) }
    val beadPath = remember(shape, sizePx) {
        createBeadPath(shape, Size(sizePx, sizePx), layoutDirection, density)
    }
    val brushCache = remember(style, sizePx) {
        BeadBrushCache(
            noiseBrush = if (style.textureStyle != BeadTextureStyle.SOLID) noiseBrush else null,
            specularBrush = buildSpecularBrush(style, pathSize),
            metallicBrush = buildMetallicBrush(style, pathSize),
        )
    }
    Canvas(modifier = Modifier.size(size.dp)) {
        drawPremiumBead(beadPath, style, brushCache)
        // Show first character of engraving as a small overlay
        if (style.engravingText.isNotBlank()) {
            drawContext.canvas.nativeCanvas.drawText(
                style.engravingText.take(1),
                sizePx / 2f,
                sizePx / 2f + sizePx * 0.12f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(200, 255, 255, 255)
                    textSize = sizePx * 0.4f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}