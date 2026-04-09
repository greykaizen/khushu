package com.kaizen.khushu.data

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

enum class BeadStyle { CLASSIC_AMBER, DARK_ONYX }

@Serializable
sealed interface TasbihWidget {
    val id: String
    val offsetX: Float  // 0.0–1.0 normalized — multiplied by screen width at render time
    val offsetY: Float  // 0.0–1.0 normalized — multiplied by screen height at render time
    val scale: Float
    val zIndex: Float

    /** The string + bead simulation. Renders on the right side of screen. */
    @Serializable
    data class StringBeadWidget(
        override val id: String = "string_bead",
        override val offsetX: Float = 0.88f,
        override val offsetY: Float = 0.5f,
        override val scale: Float = 1f,
        override val zIndex: Float = 0f,
        val beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
    ) : TasbihWidget

    /** Shows current dhikr name (Arabic text). Renders top-left. */
    @Serializable
    data class DhikrNameWidget(
        override val id: String = "dhikr_name",
        override val offsetX: Float = 0.2f,
        override val offsetY: Float = 0.15f,
        override val scale: Float = 1f,
        override val zIndex: Float = 1f,
    ) : TasbihWidget

    /** Shows count + "out of N". Renders center-left. */
    @Serializable
    data class CounterWidget(
        override val id: String = "counter",
        override val offsetX: Float = 0.15f,
        override val offsetY: Float = 0.5f,
        override val scale: Float = 1f,
        override val zIndex: Float = 1f,
    ) : TasbihWidget
}

data class TasbihCanvasPreset(
    val id: String,
    val name: String,
    val widgets: List<TasbihWidget>,
)

val DefaultTasbihPreset = TasbihCanvasPreset(
    id = "default",
    name = "Default",
    widgets = listOf(
        TasbihWidget.StringBeadWidget(),
        TasbihWidget.DhikrNameWidget(),
        TasbihWidget.CounterWidget(),
    )
)

private const val BEAD_RADIUS = 18f          // dp-independent px — scaled by density in DrawScope
private const val BOTTOM_VISIBLE_BEADS = 7   // how many uncounted beads show at the bottom
private const val TOP_GATHERED_MAX = 3        // max beads shown in top cluster

/**
 * Draws a single faux-3D bead using radial gradient lighting.
 * The light source is simulated from top-left, giving a convex sphere illusion.
 */
private fun DrawScope.drawBead(
    center: Offset,
    radius: Float,
    alpha: Float,
    style: BeadStyle = BeadStyle.CLASSIC_AMBER,
) {
    when (style) {
        BeadStyle.CLASSIC_AMBER -> {
            // Warm resin/amber sphere — orange-gold with soft warm highlight
            val lightCenter = center + Offset(-radius * 0.28f, -radius * 0.28f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFBF4D).copy(alpha = alpha),   // warm highlight
                        Color(0xFFD4850A).copy(alpha = alpha),   // mid amber
                        Color(0xFF7A4000).copy(alpha = alpha),   // deep shadow
                    ),
                    center = lightCenter,
                    radius = radius * 1.6f,
                ),
                radius = radius,
                center = center,
            )
            // Specular dot
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.65f),
                radius = radius * 0.22f,
                center = lightCenter + Offset(radius * 0.04f, radius * 0.04f),
            )
        }
        BeadStyle.DARK_ONYX -> {
            // Deep charcoal — sharp bright specular, slight oval via y-scale trick
            val lightCenter = center + Offset(-radius * 0.3f, -radius * 0.3f)
            // Slightly oval: draw as ellipse (taller than wide)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF555555).copy(alpha = alpha),   // lifted highlight
                        Color(0xFF1A1A1A).copy(alpha = alpha),   // base onyx
                        Color(0xFF000000).copy(alpha = alpha),   // deep shadow
                    ),
                    center = lightCenter,
                    radius = radius * 1.5f,
                ),
                topLeft = center + Offset(-radius, -radius * 1.08f),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2.16f),
            )
            // Sharp bright specular — smaller and brighter than amber
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.85f),
                radius = radius * 0.18f,
                center = lightCenter + Offset(radius * 0.05f, radius * 0.05f),
            )
        }
    }
}

/**
 * Renders a single TasbihWidget. Mirrors WidgetRenderer in CanvasWidget.kt.
 *
 * [stringControlXOffset] — px deflection of the Bezier control point from string center (0 = straight).
 * [stringControlYFraction] — 0.0–1.0, where along the string height the apex sits (0.5 = midpoint).
 * [countedBeads] — how many beads have been swiped up (drives top cluster + bottom remaining).
 * [totalBeads] — total count for the current dhikr item.
 * [thumbPosition] — current thumb position in Canvas-local coordinates, null when not touching.
 */
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
    thumbPosition: Offset? = null,
    modifier: Modifier = Modifier,
) {
    when (widget) {
        is TasbihWidget.StringBeadWidget -> {
            Canvas(
                modifier = modifier
                    .fillMaxHeight(0.9f)
                    .width(60.dp)
            ) {
                val density = this.density
                val beadRadius = BEAD_RADIUS * density

                val cx = size.width / 2f
                val controlX = cx + stringControlXOffset
                val controlY = size.height * stringControlYFraction

                // Build Bezier path
                val path = Path().apply {
                    moveTo(cx, 0f)
                    quadraticTo(controlX, controlY, cx, size.height)
                }

                // Draw string
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 2 * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    )
                )

                // Measure path for bead placement — use android.graphics.PathMeasure
                // which supports getPosTan, converting via asAndroidPath()
                val androidPath = path.asAndroidPath()
                val pm = android.graphics.PathMeasure(androidPath, false)
                val pathLength = pm.length
                if (pathLength <= 0f) return@Canvas

                val pos = FloatArray(2)
                val tan = FloatArray(2)

                // --- TOP CLUSTER (counted beads) ---
                val topCount = minOf(countedBeads, TOP_GATHERED_MAX)
                val topSpacing = beadRadius * 2.4f
                for (i in 0 until topCount) {
                    val dist = beadRadius + i * topSpacing
                    pm.getPosTan(dist, pos, tan)
                    val center = Offset(pos[0], pos[1])
                    val fisheyeScale = fisheyeScale(center, thumbPosition, size.width / 2f, 2.5f)
                    drawBead(center, beadRadius * fisheyeScale, alpha = 1f, style = beadStyle)
                }

                // --- BOTTOM POOL (uncounted beads) ---
                val remaining = (totalBeads - countedBeads).coerceAtLeast(0)
                val visibleBottom = minOf(remaining, BOTTOM_VISIBLE_BEADS)
                val bottomSpacing = beadRadius * 2.4f
                for (i in 0 until visibleBottom) {
                    val dist = pathLength - beadRadius - i * bottomSpacing
                    if (dist <= 0f) break
                    pm.getPosTan(dist, pos, tan)
                    val center = Offset(pos[0], pos[1])
                    // Fade out beads deeper in the stack
                    val alpha = if (i < 3) 1f else 1f - ((i - 2f) / (visibleBottom - 2f)) * 0.6f
                    val fisheyeScale = fisheyeScale(center, thumbPosition, size.width / 2f, 2.5f)
                    drawBead(center, beadRadius * fisheyeScale, alpha = alpha.coerceIn(0.2f, 1f), style = beadStyle)
                }
            }
        }

        is TasbihWidget.DhikrNameWidget -> {
            Text(
                text = currentItem?.name ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = modifier,
            )
        }

        is TasbihWidget.CounterWidget -> {
            Column(modifier = modifier) {
                Text(
                    text = currentCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                    color = Color.White.copy(alpha = 0.5f),
                )
                Text(
                    text = "out of ${currentItem?.targetCount ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.3f),
                )
            }
        }
    }
}

/**
 * Fisheye magnification: beads near the thumb swell up to [maxScale]x.
 * Falls off linearly over [radiusPx] distance.
 */
private fun fisheyeScale(
    beadCenter: Offset,
    thumb: Offset?,
    radiusPx: Float,
    maxScale: Float,
): Float {
    if (thumb == null) return 1f
    val dx = beadCenter.x - thumb.x
    val dy = beadCenter.y - thumb.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist >= radiusPx) return 1f
    return 1f + (maxScale - 1f) * (1f - dist / radiusPx)
}
