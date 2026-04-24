package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.CustomBeadStyle
import com.kaizen.khushu.data.model.DhikrItem
import kotlin.math.abs
import kotlin.math.exp
import kotlinx.serialization.Serializable

enum class BeadStyle {
    CLASSIC_AMBER,
    DARK_ONYX
}

@Serializable
sealed interface TasbihWidget {
    val id: String
    val offsetX: Float
    val offsetY: Float
    val scale: Float
    val zIndex: Float
    val width: Float
    val height: Float
    val color: Long
    val alpha: Float

    /** The vertical string of beads. Renders center-right. */
    @Serializable
    data class StringBeadWidget(
            override val id: String = "string",
            override val offsetX: Float = 0.88f,
            override val offsetY: Float = 0.5f,
            override val scale: Float = 1f,
            override val zIndex: Float = 0f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 1f,
            val beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
            val topStackLimit: Int = 3,
            val bottomStackLimit: Int = 7,
            val beadSizeScale: Float = 1.0f,
            val stringElasticity: Float = 1.8f,
            val wobbleStiffness: Float = 140f,
            val wobbleDampingRatio: Float = 0.25f,
            val beadMicroScale: Float = 1.5f,
    ) : TasbihWidget

    /** Shows the name of the current dhikr. Renders top-left. */
    @Serializable
    data class DhikrNameWidget(
            override val id: String = "name",
            override val offsetX: Float = 0.2f,
            override val offsetY: Float = 0.15f,
            override val scale: Float = 1f,
            override val zIndex: Float = 1f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 1f,
            val isBold: Boolean = true,
            val hasOutline: Boolean = false,
    ) : TasbihWidget

    /** Shows count + "out of N". Renders center-left. */
    @Serializable
    data class CounterWidget(
            override val id: String = "counter",
            override val offsetX: Float = 0.15f,
            override val offsetY: Float = 0.5f,
            override val scale: Float = 1f,
            override val zIndex: Float = 1f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 1f,
            val isBold: Boolean = false,
            val hasOutline: Boolean = false,
    ) : TasbihWidget

    /** A circular progress indicator. Renders around counter. */
    @Serializable
    data class ProgressCircleWidget(
            override val id: String = "progress_circle",
            override val offsetX: Float = 0.15f,
            override val offsetY: Float = 0.5f,
            override val scale: Float = 1f,
            override val zIndex: Float = 0.5f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 1f,
    ) : TasbihWidget

    /** Shows current dhikr meaning/translation. Renders below name. */
    @Serializable
    data class MeaningWidget(
            override val id: String = "meaning",
            override val offsetX: Float = 0.2f,
            override val offsetY: Float = 0.22f,
            override val scale: Float = 1f,
            override val zIndex: Float = 1f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 0.6f,
            val isBold: Boolean = false,
    ) : TasbihWidget

    /** A customizable text widget. */
    @Serializable
    data class CustomText(
            override val id: String,
            override val offsetX: Float = 0.5f,
            override val offsetY: Float = 0.5f,
            override val scale: Float = 1f,
            override val zIndex: Float = 1f,
            override val width: Float = 0f,
            override val height: Float = 0f,
            override val color: Long = 0xFFFFFFFF,
            override val alpha: Float = 1f,
            val text: String = "Custom Text",
            val fontSize: Float = 18f,
            val isBold: Boolean = false,
            val hasOutline: Boolean = false,
    ) : TasbihWidget
}

data class TasbihCanvasPreset(
        val id: String,
        val name: String,
        val widgets: List<TasbihWidget>,
)

val DefaultTasbihPreset =
        TasbihCanvasPreset(
                id = "default",
                name = "Default",
                widgets =
                        listOf(
                                TasbihWidget.StringBeadWidget(),
                                TasbihWidget.DhikrNameWidget(),
                                TasbihWidget.CounterWidget(),
                        )
        )

internal const val BEAD_RADIUS_BASE = 18f

private val SharedScratchPath = androidx.compose.ui.graphics.Path()
private val SharedEngravingPaint = android.graphics.Paint().apply {
    color = android.graphics.Color.argb(210, 255, 255, 255)
    textAlign = android.graphics.Paint.Align.CENTER
    isAntiAlias = true
    typeface = android.graphics.Typeface.DEFAULT_BOLD
}

@Composable
fun TasbihWidgetRenderer(
        widget: TasbihWidget,
        currentCount: Int,
        currentItem: DhikrItem?,
        stringControlXOffset: Float = 0f,
        stringControlYFraction: Float = 0.5f,
        countedBeads: Int = 0,
        totalBeads: Int = 33,
        beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
        customBeadStyle: CustomBeadStyle? = null,
        thumbPositionProvider: () -> Offset? = { null },
        // Fisheye strength (0..1): multiply peak scale and glow to fade smoothly on lift.
        fisheyeStrengthProvider: () -> Float = { 1f },
        // Transit bead: canvas-local Y of the bead being dragged between stacks, null = none.
        transitBeadProvider: () -> Float? = { null },
        transitFromBottom: Boolean = true,
        // Conveyor-belt scroll offset in canvas-local px: applied to all bead Y positions.
        // Positive = beads shifted downward; animates to 0 for slide-up effect on increment.
        scrollOffsetProvider: () -> Float = { 0f },
        modifier: Modifier = Modifier,
) {
    val baseModifier = modifier.graphicsLayer { alpha = widget.alpha }

    // Shader/Brush cache for custom beads
    val noiseShader = GlobalNoiseShader.value
    val noiseBrush = remember(noiseShader) { ShaderBrush(noiseShader) }

    // beadShapeTypeToShape is @Composable — call it at composable scope directly (Compose caches
    // internally)
    val customShape: Shape? =
            if (customBeadStyle != null) {
                beadShapeTypeToShape(customBeadStyle.shapeType)
            } else null

    when (widget) {
        is TasbihWidget.StringBeadWidget -> {
            val topLimit = widget.topStackLimit
            val bottomLimit = widget.bottomStackLimit
            val beadRadiusBase = BEAD_RADIUS_BASE * widget.beadSizeScale

            Canvas(modifier = baseModifier.fillMaxHeight(0.9f).width(120.dp)) {
                val beadRadius = beadRadiusBase * density
                // ─── TUNABLE: Base gap between beads ────────────────────────────────
                //   Higher multiplier (e.g. 0.8f) → beads spaced further apart
                //   Lower  multiplier (e.g. 0.2f) → beads tightly packed
                val gap = beadRadius * 0.4f
                val cx = size.width / 2f

                // Read all providers at draw-phase — only the canvas draw layer is invalidated.
                val fingerY: Float? = thumbPositionProvider()?.let { it.y + size.height / 2f }
                // fisheyeStrength fades 1→0 on finger lift — drives glow and scale fade-out.
                val fisheyeStrength = fisheyeStrengthProvider()
                // scrollOffset shifts all bead positions for the conveyor-belt slide animation.
                val scrollOffset = scrollOffsetProvider()

                // ─── TUNABLE: Gaussian wave width ───────────────────────────────────
                // sigma controls how far the magnification spreads from the finger.
                //   Higher (e.g. 5f) → wave covers more beads, softer peak
                //   Lower  (e.g. 2f) → tight spotlight, only 1-2 beads affected
                val sigma = beadRadius * 4f

                // Base string — thin so the glow dominates.
                drawLine(
                        color = Color.White.copy(alpha = 0.25f * widget.alpha),
                        start = Offset(cx, 0f),
                        end = Offset(cx, size.height),
                        strokeWidth = 1.5f * density,
                        cap = StrokeCap.Round,
                )

                // Double-layer string glow: tight bright core + wide soft outer halo.
                fingerY?.let { fy ->
                    // ─── TUNABLE: Outer halo (wide, soft) ─────────────────────────
                    //   outerHalf multiplier (4f) → how long the fade extends (higher = longer)
                    //   strokeWidth (28f)          → how wide the halo is (higher = fatter bloom)
                    //   alpha (0.25f)              → brightness of outer glow
                    val outerHalf = sigma * 4f
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.25f * widget.alpha * fisheyeStrength),
                                Color.Transparent,
                            ),
                            startY = (fy - outerHalf).coerceAtLeast(0f),
                            endY = (fy + outerHalf).coerceAtMost(size.height),
                        ),
                        start = Offset(cx, (fy - outerHalf).coerceAtLeast(0f)),
                        end = Offset(cx, (fy + outerHalf).coerceAtMost(size.height)),
                        strokeWidth = 28f * density,
                        cap = StrokeCap.Round,
                    )
                    // ─── TUNABLE: Inner core (narrow, bright) ───────────────────
                    //   innerHalf multiplier (2f) → length of inner glow (lower = tighter)
                    //   strokeWidth (10f)          → width of the bright streak
                    //   alpha (0.9f)               → peak brightness at centre
                    val innerHalf = sigma * 2f
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.9f * widget.alpha * fisheyeStrength),
                                Color.Transparent,
                            ),
                            startY = (fy - innerHalf).coerceAtLeast(0f),
                            endY = (fy + innerHalf).coerceAtMost(size.height),
                        ),
                        start = Offset(cx, (fy - innerHalf).coerceAtLeast(0f)),
                        end = Offset(cx, (fy + innerHalf).coerceAtMost(size.height)),
                        strokeWidth = 10f * density,
                        cap = StrokeCap.Round,
                    )
                }

                val topCount = minOf(countedBeads, topLimit).coerceAtLeast(0)
                val bottomCount = minOf(totalBeads - countedBeads, bottomLimit).coerceAtLeast(0)

                // During transit, source stack shows one fewer bead (the one is in transit).
                // Read at draw-phase — no recomposition.
                val transitCanvasY: Float? = transitBeadProvider()
                val effectiveTopCount    = if (!transitFromBottom && transitCanvasY != null) (topCount    - 1).coerceAtLeast(0) else topCount
                val effectiveBottomCount = if ( transitFromBottom && transitCanvasY != null) (bottomCount - 1).coerceAtLeast(0) else bottomCount

                // ─── TUNABLE: Peak zoom per bead ─────────────────────────────────
                // beadMicroScale (set in data class, default 1.5f) = max scale factor at peak.
                //   Higher (e.g. 3.0f) → extreme zoom, bead much larger under finger
                //   Lower  (e.g. 1.4f) → subtle magnification
                // The exponent divisor (1.5f below) sharpens/softens the falloff:
                //   Higher divisor (e.g. 3.0f) → softer, more gradual falloff from peak
                //   Lower  divisor (e.g. 0.8f) → very sharp peak, falls off quickly
                fun beadScale(naturalY: Float): Float {
                    fingerY ?: return 1f
                    val dist = kotlin.math.abs(naturalY - fingerY)
                    val gaussian = kotlin.math.exp(-(dist * dist) / (1.5f * sigma * sigma)).toFloat()
                    return 1f + (widget.beadMicroScale - 1f) * gaussian * fisheyeStrength
                }

                // ─── TUNABLE: Gap stretch at peak ───────────────────────────────
                // The 0.5f multiplier controls how much extra space opens between beads near the finger.
                //   Higher (e.g. 1.0f) → beads push further apart, very dock-like spread
                //   0.0f               → gap stays fixed regardless of zoom
                fun scaledGap(scaleA: Float, scaleB: Float) = gap * (1f + 0.5f * ((scaleA + scaleB) / 2f - 1f))

                // ── Top stack ──────────────────────────────────────────────────────
                val topNaturalYs = FloatArray(effectiveTopCount) { i ->
                    beadRadius + i * (beadRadius * 2f + gap)
                }
                val topScales = FloatArray(effectiveTopCount) { i -> beadScale(topNaturalYs[i]) }
                val topCenterYs = FloatArray(effectiveTopCount)
                if (effectiveTopCount > 0) {
                    topCenterYs[0] = beadRadius * topScales[0]
                    for (i in 1 until effectiveTopCount) {
                        topCenterYs[i] = topCenterYs[i - 1] + beadRadius * topScales[i - 1] +
                            scaledGap(topScales[i - 1], topScales[i]) + beadRadius * topScales[i]
                    }
                }
                for (i in 0 until effectiveTopCount) {
                    val r = beadRadius * topScales[i]
                    val cy = topCenterYs[i] + scrollOffset  // scroll offset shifts all top-stack beads
                    if (topScales[i] > 1.15f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.18f * widget.alpha * (topScales[i] - 1f)), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = r * 1.8f,
                            ),
                            radius = r * 1.8f,
                            center = Offset(cx, cy),
                        )
                    }
                    drawBeadWrapper(Offset(cx, cy), r, beadStyle, customBeadStyle, customShape, noiseBrush)
                }

                // ── Bottom stack ────────────────────────────────────────────────────
                val bottomNaturalYs = FloatArray(effectiveBottomCount) { i ->
                    size.height - beadRadius - (effectiveBottomCount - 1 - i) * (beadRadius * 2f + gap)
                }
                val bottomScales = FloatArray(effectiveBottomCount) { i -> beadScale(bottomNaturalYs[i]) }
                val bottomCenterYs = FloatArray(effectiveBottomCount)
                if (effectiveBottomCount > 0) {
                    bottomCenterYs[effectiveBottomCount - 1] =
                        size.height - beadRadius * bottomScales[effectiveBottomCount - 1]
                    for (i in effectiveBottomCount - 2 downTo 0) {
                        bottomCenterYs[i] = bottomCenterYs[i + 1] - beadRadius * bottomScales[i + 1] -
                            scaledGap(bottomScales[i + 1], bottomScales[i]) - beadRadius * bottomScales[i]
                    }
                }
                for (i in 0 until effectiveBottomCount) {
                    val r = beadRadius * bottomScales[i]
                    val cy = bottomCenterYs[i] + scrollOffset  // scroll offset shifts all bottom-stack beads
                    if (bottomScales[i] > 1.15f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.18f * widget.alpha * (bottomScales[i] - 1f)), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = r * 1.8f,
                            ),
                            radius = r * 1.8f,
                            center = Offset(cx, cy),
                        )
                    }
                    drawBeadWrapper(Offset(cx, cy), r, beadStyle, customBeadStyle, customShape, noiseBrush)
                }

                // ── Transit bead: drawn on top, follows finger across the gap ───────
                // Visible only while a bead is being dragged between stacks.
                transitCanvasY?.let { ty ->
                    // Always at least 1.5× to signal it is "lifted" from its stack.
                    val tScale = beadScale(ty).coerceAtLeast(1.5f)
                    val tr = beadRadius * tScale
                    // Large glowing halo so the held bead stands out clearly.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.45f * widget.alpha), Color.Transparent),
                            center = Offset(cx, ty),
                            radius = tr * 2.5f,
                        ),
                        radius = tr * 2.5f,
                        center = Offset(cx, ty),
                    )
                    drawBeadWrapper(Offset(cx, ty), tr, beadStyle, customBeadStyle, customShape, noiseBrush)
                }
            }
        }


        is TasbihWidget.DhikrNameWidget -> {
            Box(modifier = baseModifier) {
                Text(
                        text = currentItem?.name ?: "سُبْحَانَ اللَّهِ",
                        style =
                                TextStyle(
                                        color = Color(widget.color),
                                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                        fontWeight =
                                                if (widget.isBold) FontWeight.Bold
                                                else FontWeight.Normal,
                                        drawStyle =
                                                if (widget.hasOutline)
                                                        Stroke(width = 4f, join = StrokeJoin.Round)
                                                else Fill,
                                        platformStyle =
                                                PlatformTextStyle(includeFontPadding = false)
                                )
                )
            }
        }
        is TasbihWidget.CounterWidget -> {
            Column(modifier = baseModifier) {
                Text(
                        text = currentCount.toString(),
                        style =
                                TextStyle(
                                        color = Color(widget.color),
                                        fontSize = 96.sp,
                                        fontWeight =
                                                if (widget.isBold) FontWeight.Bold
                                                else FontWeight.Normal,
                                        drawStyle =
                                                if (widget.hasOutline)
                                                        Stroke(width = 6f, join = StrokeJoin.Round)
                                                else Fill,
                                        platformStyle =
                                                PlatformTextStyle(includeFontPadding = false)
                                )
                )
                Text(
                        text = "out of ${currentItem?.targetCount ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(widget.color).copy(alpha = 0.5f),
                )
            }
        }
        is TasbihWidget.ProgressCircleWidget -> {
            val progress = currentCount.toFloat() / (currentItem?.targetCount?.toFloat() ?: 1f)
            Canvas(modifier = baseModifier.size(240.dp)) {
                drawCircle(
                        color = Color.White.copy(alpha = 0.05f * widget.alpha),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                        color = Color(widget.color),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        is TasbihWidget.MeaningWidget -> {
            Text(
                    text = "Glorified is Allah",
                    style =
                            TextStyle(
                                    color = Color(widget.color).copy(alpha = 0.6f),
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    fontWeight =
                                            if (widget.isBold) FontWeight.Bold
                                            else FontWeight.Normal,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                    modifier = baseModifier,
            )
        }
        is TasbihWidget.CustomText -> {
            Text(
                    text = widget.text,
                    style =
                            TextStyle(
                                    color = Color(widget.color),
                                    fontSize = widget.fontSize.sp,
                                    fontWeight =
                                            if (widget.isBold) FontWeight.Bold
                                            else FontWeight.Normal,
                                    drawStyle =
                                            if (widget.hasOutline)
                                                    Stroke(width = 4f, join = StrokeJoin.Round)
                                            else Fill,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                    modifier = baseModifier
            )
        }
    }
}

internal fun DrawScope.drawBeadWrapper(
        center: Offset,
        radius: Float,
        legacyStyle: BeadStyle,
        customStyle: CustomBeadStyle?,
        customShape: Shape?,
        noiseBrush: ShaderBrush?,
) {
    if (customStyle != null && customShape != null) {
        val sizePx = radius * 2f
        updateBeadPath(
                customShape,
                androidx.compose.ui.geometry.Size(sizePx, sizePx),
                layoutDirection,
                androidx.compose.ui.unit.Density(density),
                SharedScratchPath
        )
        translate(left = center.x - radius, top = center.y - radius) {
            drawPremiumBead(SharedScratchPath, customStyle, noiseBrush)
        }
    } else {
        drawBead(center, radius, 1f, legacyStyle)
    }
}

fun DrawScope.drawPremiumBead(
        path: Path,
        style: CustomBeadStyle,
        noiseBrush: ShaderBrush?,
        drawEngraving: Boolean = true
) {
    val bounds = path.getBounds()
    val pathSize = androidx.compose.ui.geometry.Size(bounds.width, bounds.height)

    // 1. Extrusion
    if (style.is3dEnabled) drawBeadExtrusion(path, style)

    // 2. Base color
    drawBeadBaseColor(path, style)

    // 3. Texture
    drawBeadTexture(path, style, noiseBrush)

    // 4. Specular highlight
    val specularBrush = buildSpecularBrush(style, pathSize)
    drawBeadSpecular(path, specularBrush)

    // 5. Chromatic aberration
    drawBeadChromaticAberration(path, style)

    // 6. Metallic sheen
    val metallicBrush = buildMetallicBrush(style, pathSize)
    drawBeadMetallicSheen(path, metallicBrush)

    // 7. Engraving text — drawn last so it sits on top of all effects
    // Skip in preview mode; BeadPreview composable renders its own Text layer with gesture support
    if (drawEngraving && style.engravingText.isNotBlank()) {
        val textSize = bounds.width * 0.38f
        SharedEngravingPaint.textSize = textSize
        // Apply user scale and offset (stored as dp-equivalent values)
        val cx = bounds.left + bounds.width / 2f + (style.textOffsetX * density)
        val cy = bounds.top + bounds.height / 2f + (style.textOffsetY * density) + textSize * 0.35f
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.scale(style.textScale, style.textScale, cx, cy)
        drawContext.canvas.nativeCanvas.drawText(style.engravingText, cx, cy, SharedEngravingPaint)
        drawContext.canvas.nativeCanvas.restore()
    }
}

private fun DrawScope.drawBead(
        center: Offset,
        radius: Float,
        alpha: Float,
        style: BeadStyle = BeadStyle.CLASSIC_AMBER,
) {
    when (style) {
        BeadStyle.CLASSIC_AMBER -> {
            val lightCenter = center + Offset(-radius * 0.28f, -radius * 0.28f)
            drawCircle(
                    brush =
                            Brush.radialGradient(
                                    colors =
                                            listOf(
                                                    Color(0xFFFFBF4D).copy(alpha = alpha),
                                                    Color(0xFFD4850A).copy(alpha = alpha),
                                                    Color(0xFF7A4000).copy(alpha = alpha),
                                            ),
                                    center = lightCenter,
                                    radius = radius * 1.6f,
                            ),
                    radius = radius,
                    center = center,
            )
            drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.65f),
                    radius = radius * 0.22f,
                    center = lightCenter + Offset(radius * 0.04f, radius * 0.04f),
            )
        }
        BeadStyle.DARK_ONYX -> {
            val lightCenter = center + Offset(-radius * 0.3f, -radius * 0.3f)
            drawOval(
                    brush =
                            Brush.radialGradient(
                                    colors =
                                            listOf(
                                                    Color(0xFF555555).copy(alpha = alpha),
                                                    Color(0xFF1A1A1A).copy(alpha = alpha),
                                                    Color(0xFF000000).copy(alpha = alpha),
                                            ),
                                    center = lightCenter,
                                    radius = radius * 1.5f,
                            ),
                    topLeft = center + Offset(-radius, -radius * 1.08f),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2.16f),
            )
            drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.85f),
                    radius = radius * 0.18f,
                    center = lightCenter + Offset(radius * 0.05f, radius * 0.05f),
            )
        }
    }
}
