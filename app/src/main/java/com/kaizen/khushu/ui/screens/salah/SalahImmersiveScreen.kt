package com.kaizen.khushu.ui.screens.salah

import android.app.Activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.ui.theme.Antonio
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.CanvasWidget
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val SWIPE_THRESHOLD = 80.dp

@Composable
fun SalahImmersiveScreen(
    targetRakats: Int,
    preset: SalahPreset,
    salahCanvasViewModel: SalahCanvasViewModel,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    // Hide status bar + nav bar for full immersive focus
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

    var count by remember { mutableIntStateOf(0) }
    var showOverlay by remember { mutableStateOf(false) }
    val isComplete = count >= targetRakats

    val density = LocalDensity.current
    val swipeThresholdPx = remember(density) { with(density) { SWIPE_THRESHOLD.toPx() } }
    var swipeAccum by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            delay(5_000)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Tap = count up / dismiss overlay; long press = toggle overlay
            .pointerInput(isComplete, showOverlay) {
                detectTapGestures(
                    onTap = {
                        if (showOverlay) {
                            showOverlay = false
                        } else if (!isComplete) {
                            count++
                        }
                    },
                    onLongPress = { if (!isComplete) showOverlay = !showOverlay },
                )
            }
            // Swipe down = exit (draggable applied after pointerInput = higher event priority)
            .draggable(
                state = rememberDraggableState { delta ->
                    if (delta > 0) swipeAccum += delta else swipeAccum = 0f
                },
                orientation = Orientation.Vertical,
                onDragStarted = { swipeAccum = 0f },
                onDragStopped = {
                    if (swipeAccum > swipeThresholdPx) onExit()
                    swipeAccum = 0f
                },
            ),
    ) {
        // Preset layout — add new presets here
        when (preset) {
            SalahPreset.Minimal -> MinimalPresetLayout(
                count = count,
                isComplete = isComplete,
            )
            SalahPreset.Custom -> CustomPresetLayout(
                count = count,
                isComplete = isComplete,
                viewModel = salahCanvasViewModel,
            )
        }

        // Long-press overlay pill
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
        ) {
            Text(
                text = "Hold to Reset  ·  Swipe Down to Cancel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(50),
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MinimalPresetLayout(
    count: Int,
    isComplete: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Switch between counting and completion — outer transition on isComplete
        AnimatedContent(
            targetState = isComplete,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "completionSwitch",
        ) { complete ->
            if (complete) {
                // Completion state — fade in final count + label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        fontFamily = Antonio, // Explicitly set Antonio font
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Salah Complete",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            } else {
                // Counting state — animate each rakat increment
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.75f) + fadeIn()) togetherWith
                                (scaleOut(targetScale = 1.15f) + fadeOut())
                    },
                    label = "rakatCount",
                ) { displayCount ->
                    Text(
                        text = displayCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        fontFamily = Antonio, // Explicitly set Antonio font
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomPresetLayout(
    count: Int,
    isComplete: Boolean,
    viewModel: SalahCanvasViewModel,
) {
    val layout by viewModel.layout.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(layout.backgroundColorInt.toInt())),
    ) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        layout.widgets.forEach { widget ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            widget.offsetX.roundToInt(),
                            widget.offsetY.roundToInt(),
                        )
                    }
                    .graphicsLayer(scaleX = widget.scale, scaleY = widget.scale)
            ) {
                when (widget) {
                    is CanvasWidget.RakatCount -> {
                        Text(
                            text = if (isComplete) "Finish" else count.toString(),
                            fontFamily = Antonio,
                            fontSize = widget.fontSizeSp.sp,
                            color = Color(widget.color),
                        )
                    }
                    is CanvasWidget.ClockWidget -> {
                        LiveClockText(widget)
                    }
                    is CanvasWidget.CustomText -> {
                        Text(
                            text = widget.text,
                            fontSize = widget.fontSizeSp.sp,
                            color = Color(widget.color),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveClockText(widget: CanvasWidget.ClockWidget) {
    var timeStr by remember { mutableStateOf("") }
    LaunchedEffect(widget.showSeconds, widget.use24Hour) {
        while (true) {
            val cal = java.util.Calendar.getInstance()
            val h = if (widget.use24Hour) cal.get(java.util.Calendar.HOUR_OF_DAY) 
                    else cal.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
            val m = cal.get(java.util.Calendar.MINUTE)
            val s = cal.get(java.util.Calendar.SECOND)
            timeStr = if (widget.showSeconds) "%02d:%02d:%02d".format(h, m, s)
                      else "%02d:%02d".format(h, m)
            delay(1000)
        }
    }
    Text(timeStr, fontSize = widget.fontSizeSp.sp, color = Color(widget.color))
}
