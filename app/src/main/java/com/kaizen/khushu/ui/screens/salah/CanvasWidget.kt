package com.kaizen.khushu.ui.screens.salah

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import kotlinx.serialization.Serializable
import java.time.LocalTime

fun String.resolveFontFamily(): FontFamily {
    return when (this) {
        "Antonio" -> Antonio
        "BeVietnamPro" -> BeVietnamPro
        else -> FontFamily.Default
    }
}

@Composable
fun WidgetRenderer(
    widget: CanvasWidget,
    currentRakats: Int,
    isComplete: Boolean,
    completionText: String = "الحمد لله",
    modifier: Modifier = Modifier
) {
    val widgetFontFamily = when (widget) {
        is CanvasWidget.RakatCount -> widget.fontName.resolveFontFamily()
        is CanvasWidget.ClockWidget -> widget.fontName.resolveFontFamily()
        is CanvasWidget.CustomText -> widget.fontName.resolveFontFamily()
    }

    when (widget) {
        is CanvasWidget.RakatCount -> {
            Box(
                modifier = if (isComplete) modifier.fillMaxWidth(0.82f) else modifier,
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isComplete,
                    transitionSpec = {
                        fadeIn(tween(800)) togetherWith
                        fadeOut(tween(800)) using
                        SizeTransform(clip = false)
                    },
                    contentAlignment = Alignment.Center,
                    label = "completion_crossfade",
                ) { complete ->
                    Text(
                        text = if (complete) completionText else currentRakats.toString(),
                        fontFamily = widgetFontFamily,
                        fontSize = if (complete) (widget.fontSizeSp * 0.30f).sp else widget.fontSizeSp.sp,
                        fontWeight = FontWeight(widget.fontWeight),
                        textAlign = TextAlign.Center,
                        maxLines = if (complete) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            color = Color(widget.color).copy(alpha = widget.opacity),
                            drawStyle = if (widget.isOutline) Stroke(width = 4f, join = StrokeJoin.Round) else Fill,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }
        is CanvasWidget.ClockWidget -> {
            var timeStr by remember { mutableStateOf("") }
            LaunchedEffect(widget.showSeconds, widget.use24Hour) {
                while (true) {
                    val now = LocalTime.now()
                    val h = if (widget.use24Hour) now.hour else now.hour.let { if (it == 0) 12 else it }
                    val m = now.minute
                    val s = now.second
                    timeStr = if (widget.showSeconds) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(h, m)
                    kotlinx.coroutines.delay(1000)
                }
            }
            Text(
                text = timeStr,
                fontFamily = widgetFontFamily,
                fontSize = widget.fontSizeSp.sp,
                style = TextStyle(
                    color = Color(widget.color).copy(alpha = widget.opacity),
                    drawStyle = if (widget.isOutline) Stroke(width = 4f, join = StrokeJoin.Round) else Fill,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = modifier
            )
        }
        is CanvasWidget.CustomText -> {
            Box(
                contentAlignment = when (widget.verticalAlign) {
                    "Top" -> Alignment.TopCenter
                    "Bottom" -> Alignment.BottomCenter
                    else -> Alignment.Center
                },
                modifier = modifier
            ) {
                Text(
                    text = widget.text ?: "",
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
                        drawStyle = if (widget.isOutline) Stroke(width = 4f, join = StrokeJoin.Round) else Fill,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Serializable
sealed interface CanvasWidget {
    val id: String
    val offsetX: Float      // absolute pixel position
    val offsetY: Float      // absolute pixel position
    val scale: Float        // 1.0f = default size
    val zIndex: Float
    val width: Float        // measured width in pixels
    val height: Float       // measured height in pixels

    @Serializable
    data class RakatCount(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 0f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 180f,
        val fontWeight: Int = 400,
        val isOutline: Boolean = false,
        val fontName: String = "Antonio",
    ) : CanvasWidget

    @Serializable
    data class ClockWidget(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 1f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 48f,
        val showSeconds: Boolean = false,
        val use24Hour: Boolean = true,
        val isOutline: Boolean = false,
        val fontName: String = "BeVietnamPro",
    ) : CanvasWidget

    @Serializable
    data class CustomText(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 2f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val text: String = "Bismillah",
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 32f,
        val fontWeight: Int = 400,
        val italic: Boolean = false,
        val textAlign: String = "Center",
        val verticalAlign: String = "Center",
        val isOutline: Boolean = false,
        val fontName: String = "BeVietnamPro",
    ) : CanvasWidget
}
