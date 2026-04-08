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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

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

/**
 * Renders a single TasbihWidget. Mirrors WidgetRenderer in CanvasWidget.kt.
 * [currentCount] and [currentItem] are session state injected from TasbihPhysicalScreen.
 */
@Composable
fun TasbihWidgetRenderer(
    widget: TasbihWidget,
    currentCount: Int,
    currentItem: DhikrItem?,
    modifier: Modifier = Modifier,
) {
    when (widget) {
        is TasbihWidget.StringBeadWidget -> {
            // Phase 1: static vertical line. Phase 2+ replaces with Bezier + beads.
            Canvas(
                modifier = modifier
                    .fillMaxHeight(0.9f)
                    .width(60.dp)
            ) {
                val cx = size.width / 2f
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, size.height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
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
