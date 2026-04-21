package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.model.DhikrItem
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

enum class BeadStyle { CLASSIC_AMBER, DARK_ONYX }

@Serializable
sealed interface TasbihWidget {
    val id: String
    val offsetX: Float
    val offsetY: Float
    val scale: Float
    val zIndex: Float
    val width: Float
    val height: Float

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
        val beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
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
        val color: Long? = null,
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

private const val BEAD_RADIUS = 18f
private const val BOTTOM_VISIBLE_BEADS = 7
private const val TOP_GATHERED_MAX = 3

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
    activeBeadProgress: Float? = null,
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

                val path = Path().apply {
                    moveTo(cx, 0f)
                    quadraticTo(controlX, controlY, cx, size.height)
                }

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 2 * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    )
                )

                val androidPath = path.asAndroidPath()
                val pm = android.graphics.PathMeasure(androidPath, false)
                val pathLength = pm.length
                if (pathLength <= 0f) return@Canvas

                val pos = FloatArray(2)
                val tan = FloatArray(2)
                val fisheyeRadius = size.height * 0.12f

                var activeCenter: Offset? = null
                if (activeBeadProgress != null) {
                    val activeDist = (activeBeadProgress * pathLength).coerceIn(0f, pathLength)
                    if (pm.getPosTan(activeDist, pos, tan)) {
                        activeCenter = Offset(pos[0], pos[1])
                    }
                }

                val fisheyeCenter: Offset? = activeCenter ?: thumbPosition

                val topCount = minOf(countedBeads, TOP_GATHERED_MAX)
                val topSpacing = beadRadius * 2.4f
                for (i in 0 until topCount) {
                    val dist = beadRadius + i * topSpacing
                    pm.getPosTan(dist, pos, tan)
                    val center = Offset(pos[0], pos[1])
                    val scale = fisheyeScale(center, fisheyeCenter, fisheyeRadius, 2.5f)
                    drawBead(center, beadRadius * scale, alpha = 1f, style = beadStyle)
                }

                if (activeCenter != null) {
                    val scale = fisheyeScale(activeCenter, fisheyeCenter, fisheyeRadius, 2.5f)
                    drawBead(activeCenter, beadRadius * scale, alpha = 1f, style = beadStyle)
                }

                val remaining = (totalBeads - countedBeads).coerceAtLeast(0)
                val poolSize = if (activeBeadProgress != null) (remaining - 1).coerceAtLeast(0) else remaining
                val visibleBottom = minOf(poolSize, BOTTOM_VISIBLE_BEADS)
                val bottomSpacing = beadRadius * 2.4f
                for (i in 0 until visibleBottom) {
                    val dist = pathLength - beadRadius - i * bottomSpacing
                    if (dist <= 0f) break
                    pm.getPosTan(dist, pos, tan)
                    val center = Offset(pos[0], pos[1])
                    val alpha = if (i < 3) 1f else 1f - ((i - 2f) / (visibleBottom - 2f)) * 0.6f
                    val scale = fisheyeScale(center, fisheyeCenter, fisheyeRadius, 2.5f)
                    drawBead(center, beadRadius * scale, alpha = alpha.coerceIn(0.2f, 1f), style = beadStyle)
                }
            }
        }

        is TasbihWidget.DhikrNameWidget -> {
            Text(
                text = currentItem?.name ?: "سُبْحَانَ اللَّهِ",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = modifier
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

        is TasbihWidget.ProgressCircleWidget -> {
            val progress = currentCount.toFloat() / (currentItem?.targetCount?.toFloat() ?: 1f)
            Canvas(modifier = modifier.size(240.dp)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = widget.color?.let { Color(it) } ?: Color.White.copy(alpha = 0.3f),
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
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                modifier = modifier,
            )
        }
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
                brush = Brush.radialGradient(
                    colors = listOf(
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
                brush = Brush.radialGradient(
                    colors = listOf(
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
    val t = dist / radiusPx
    return 1f + (maxScale - 1f) * (1f - t * t)
}
