package com.kaizen.khushu.ui.screens.salah

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.data.CanvasPreset
import com.kaizen.khushu.data.CanvasWidget
import com.kaizen.khushu.data.WidgetRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SalahImmersiveScreen(
    targetRakats: Int,
    preset: CanvasPreset,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    val view = LocalView.current
    val window = (LocalContext.current as Activity).window
    DisposableEffect(Unit) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }

    // Initialize the Android Haptic Engine
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    var count by remember { mutableIntStateOf(0) }
    var resetProgress by remember { mutableFloatStateOf(0f) }
    var resetArmed by remember { mutableStateOf(false) }
    var showOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isComplete = count >= targetRakats

    LaunchedEffect(isComplete) {
        if (isComplete) {
            kotlinx.coroutines.delay(10_000)
            onComplete()
        }
    }

    val safeBackgroundColor = Color(preset.backgroundColor.toLong() and 0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(safeBackgroundColor)
            .pointerInput(isComplete) {
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        val downChange = downEvent.changes.firstOrNull { it.pressed } ?: continue

                        var hasSwiped = false
                        var localResetArmed = false // Logic state (independent of visual state)
                        var holdJob: kotlinx.coroutines.Job? = null

                        if (!isComplete) {
                            holdJob = scope.launch {
                                kotlinx.coroutines.delay(200)
                                showOverlay = true
                                resetProgress = 0f
                                resetArmed = false

                                for (i in 1..20) {
                                    kotlinx.coroutines.delay(40)
                                    resetProgress = i / 20f
                                }
                                resetArmed = true
                                localResetArmed = true // Update logic state
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }

                        // Tracking Loop
                        while (true) {
                            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
                            val change = event.changes.firstOrNull()

                            if (change == null || !change.pressed) {
                                break // Finger lifted
                            }

                            if (!hasSwiped && (change.position.y - downChange.position.y > 100f)) {
                                hasSwiped = true
                                holdJob?.cancel()

                                if (showOverlay) {
                                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                } else if (count > 0 && !isComplete) {
                                    count--
                                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                }
                            }

                            if (change.positionChanged()) {
                                change.consume()
                            }
                        }

                        // Finger Lifted
                        holdJob?.cancel()
                        val overlayWasShown = showOverlay // Capture this before hiding

                        if (!hasSwiped && !isComplete) {
                            if (localResetArmed) { // Uses the local state so rapid taps don't trigger resets
                                count = 0
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            } else if (overlayWasShown) {
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            } else {
                                count++
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                        }

                        // Trigger the slide-out animation immediately
                        showOverlay = false

                        // DELAY the visual state resets so the 500ms slide-out animation completes with the correct red/armed text
                        scope.launch {
                            kotlinx.coroutines.delay(500)
                            if (!showOverlay) { // Only clear it if a new touch hasn't started
                                resetProgress = 0f
                                resetArmed = false
                            }
                        }
                    }
                }
            }
    ) {
        preset.widgets.sortedBy { it.zIndex }.forEach { widget ->
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = widget.offsetX
                        translationY = widget.offsetY
                        scaleX = widget.scale
                        scaleY = widget.scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                WidgetRenderer(widget = widget, currentRakats = count, isComplete = isComplete)
            }
        }

        AnimatedVisibility(
            visible = showOverlay,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { -it },
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        progress = { resetProgress },
                        modifier = Modifier.size(24.dp),
                        color = if (resetArmed) MaterialTheme.colorScheme.error else Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = if (resetArmed) "Release to Reset  ·  Swipe Down to Abort" else "Holding to Reset...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (resetArmed) MaterialTheme.colorScheme.error else Color.White
                    )
                }
            }
        }

        androidx.compose.material3.OutlinedButton (
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
        ) {
            Text(
                text = "Exit",
                color = Color.White.copy(alpha = 0.2f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}