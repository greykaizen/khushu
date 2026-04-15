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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
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

enum class SessionState { ACTIVE, APEX, SLEEP, AWAKE }

@Composable
fun SalahImmersiveScreen(
    targetRakats: Int,
    preset: CanvasPreset,
    showExitButton: Boolean = true,
    showCompletionText: Boolean = true,
    completionText: String = "الحمد لله",
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
    var sessionState by remember { mutableStateOf(SessionState.ACTIVE) }
    val isComplete = sessionState != SessionState.ACTIVE

    LaunchedEffect(count) {
        if (count >= targetRakats && sessionState == SessionState.ACTIVE) {
            if (showCompletionText) {
                sessionState = SessionState.APEX
                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            } else {
                sessionState = SessionState.SLEEP
            }
        }
    }

    LaunchedEffect(sessionState) {
        if (sessionState == SessionState.APEX) {
            delay(2800)
            sessionState = SessionState.SLEEP
        }
    }

    val contentAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (sessionState == SessionState.SLEEP) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = if (sessionState == SessionState.SLEEP) 1500 else 300
        ),
        label = "oled_fade"
    )

    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(sessionState, showExitButton) {
        if (sessionState == SessionState.SLEEP && !showExitButton) {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val safeBackgroundColor = Color(preset.backgroundColor.toLong() and 0xFFFFFFFF)

    val animatedBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (sessionState == SessionState.SLEEP) Color.Black else safeBackgroundColor,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = if (sessionState == SessionState.SLEEP) 1500 else 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "bg_color_fade"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBgColor)
            .pointerInput(sessionState) {
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        val downChange = downEvent.changes.firstOrNull { it.pressed } ?: continue

                        var hasSwiped = false
                        var localResetArmed = false
                        var holdJob: kotlinx.coroutines.Job? = null

                        if (sessionState == SessionState.ACTIVE) {
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
                                localResetArmed = true
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }

                        while (true) {
                            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
                            val change = event.changes.firstOrNull()

                            if (change == null || !change.pressed) {
                                break
                            }

                            if (!hasSwiped && (change.position.y - downChange.position.y > 100f)) {
                                hasSwiped = true
                                holdJob?.cancel()

                                if (sessionState == SessionState.ACTIVE) {
                                    if (showOverlay) {
                                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    } else if (count > 0) {
                                        count--
                                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }

                            if (change.positionChanged()) {
                                change.consume()
                            }
                        }

                        holdJob?.cancel()
                        val overlayWasShown = showOverlay

                        if (sessionState == SessionState.ACTIVE && !hasSwiped) {
                            if (localResetArmed) {
                                count = 0
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            } else if (overlayWasShown) {
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            } else {
                                count++
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                        } else if (sessionState == SessionState.SLEEP && !hasSwiped) {
                            sessionState = SessionState.AWAKE
                        } else if (sessionState == SessionState.AWAKE && !hasSwiped) {
                            if (localResetArmed) {
                                count = 0
                                sessionState = SessionState.ACTIVE
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }

                        showOverlay = false

                        scope.launch {
                            kotlinx.coroutines.delay(500)
                            if (!showOverlay) {
                                resetProgress = 0f
                                resetArmed = false
                            }
                        }
                    }
                }
            }
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val widgetsAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isComplete) 0f else 1f,
            animationSpec = androidx.compose.animation.core.tween(800),
            label = "widgets_alpha"
        )
        val completionAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isComplete) 1f else 0f,
            animationSpec = androidx.compose.animation.core.tween(800),
            label = "completion_alpha"
        )

        Box(modifier = Modifier.alpha(contentAlpha)) {
            preset.widgets.sortedBy { it.zIndex }.forEach { widget ->
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = (widget.offsetX * screenWidth) - (size.width / 2f)
                            translationY = (widget.offsetY * screenHeight) - (size.height / 2f)
                            scaleX = widget.scale
                            scaleY = widget.scale
                            transformOrigin = TransformOrigin.Center
                            alpha = widgetsAlpha
                        }
                ) {
                    WidgetRenderer(widget = widget, currentRakats = count, isComplete = false, completionText = completionText)
                }
            }

            // Completion text — always centered regardless of preset layout
            preset.widgets.filterIsInstance<CanvasWidget.RakatCount>().firstOrNull()?.let { rakatWidget ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(completionAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    WidgetRenderer(
                        widget = rakatWidget,
                        currentRakats = count,
                        isComplete = isComplete,
                        completionText = completionText
                    )
                }
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

        AnimatedVisibility(
            visible = (showExitButton && sessionState != SessionState.SLEEP) || sessionState == SessionState.AWAKE,
            enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)),
            exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onExit,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "Exit",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}