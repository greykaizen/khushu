package com.kaizen.khushu.ui.screens.tasbeeh

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.data.BeadStyle
import com.kaizen.khushu.data.DefaultTasbihPreset
import com.kaizen.khushu.data.TasbihCanvasPreset
import com.kaizen.khushu.data.TasbihWidget
import com.kaizen.khushu.data.TasbihWidgetRenderer
import com.kaizen.khushu.data.TasbeehCollection
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.sqrt

// Must match BEAD_RADIUS in TasbihWidget.kt
private const val BEAD_RADIUS_DP = 18f

@Composable
fun TasbeehImmersiveScreen(
    collection: TasbeehCollection,
    onExit: () -> Unit,
    preset: TasbihCanvasPreset = DefaultTasbihPreset,
    beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val haptics = LocalHapticFeedback.current

    // Hide system bars
    DisposableEffect(Unit) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // --- Session state ---
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var currentCount by remember { mutableIntStateOf(0) }
    val items = collection.items
    val currentItem = items.getOrNull(currentItemIndex)
    val currentTarget = currentItem?.targetCount ?: 33

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    // --- Bezier spring state ---
    val controlXAnim = remember { Animatable(0f) }
    val controlYAnim = remember { Animatable(0.5f) }
    val scope = rememberCoroutineScope()
    val stringSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    // --- Bead drag state ---
    // 1.0 = bead at bottom (uncounted pool), 0.0 = bead at top (counted cluster)
    val activeBeadProgress = remember { Animatable(1f) }
    var isBeadDragActive by remember { mutableStateOf(false) }

    // --- Gesture / interaction state ---
    var thumbPosition by remember { mutableStateOf<Offset?>(null) }
    var showExitOverlay by remember { mutableStateOf(false) }

    fun countUp() {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        currentCount++
        if (currentCount >= currentTarget) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (currentItemIndex < items.lastIndex) {
                currentItemIndex++
                currentCount = 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        // --- Widget rendering ---
        if (screenWidth > 0f && screenHeight > 0f) {
            preset.widgets.sortedBy { it.zIndex }.forEach { widget ->
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = (widget.offsetX * screenWidth) - (size.width / 2f)
                        translationY = (widget.offsetY * screenHeight) - (size.height / 2f)
                        scaleX = widget.scale
                        scaleY = widget.scale
                        transformOrigin = TransformOrigin.Center
                    }
                ) {
                    TasbihWidgetRenderer(
                        widget = widget,
                        currentCount = currentCount,
                        currentItem = currentItem,
                        stringControlXOffset = controlXAnim.value,
                        stringControlYFraction = controlYAnim.value,
                        countedBeads = currentCount,
                        totalBeads = currentTarget,
                        beadStyle = beadStyle,
                        activeBeadProgress = if (widget is TasbihWidget.StringBeadWidget && isBeadDragActive)
                            activeBeadProgress.value else null,
                        thumbPosition = if (widget is TasbihWidget.StringBeadWidget) {
                            // Translate screen thumb into string-widget-local coords
                            thumbPosition?.let { t ->
                                val widgetScreenX = widget.offsetX * screenWidth
                                val widgetScreenY = widget.offsetY * screenHeight
                                Offset(t.x - widgetScreenX, t.y - widgetScreenY)
                            }
                        } else null,
                    )
                }
            }
        }

        // --- Right-half interaction zone ---
        if (screenWidth > 0f && screenHeight > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .pointerInput(screenWidth, screenHeight) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()

                            // --- Hit-test: is touch near the bottom bead? ---
                            // String widget: offsetX=0.88, fillMaxHeight(0.9f), centered at screenHeight*0.5
                            // Bottom bead sits near screenHeight*0.95 in screen coords
                            val beadRadiusPx = BEAD_RADIUS_DP * density
                            val stringScreenX = 0.88f * screenWidth
                            val stringBottomScreenY = screenHeight * 0.95f - beadRadiusPx

                            // Touch in absolute screen coords (gesture box covers right half)
                            val touchScreenX = down.position.x + screenWidth * 0.5f
                            val touchScreenY = down.position.y

                            val dx = touchScreenX - stringScreenX
                            val dy = touchScreenY - stringBottomScreenY
                            val hitRadius = 48f * density  // 48dp hit target
                            val isBeadHit = sqrt(dx * dx + dy * dy) < hitRadius

                            val startY = down.position.y
                            var hasCounted = false

                            val holdJob = scope.launch {
                                kotlinx.coroutines.delay(350)
                                if (!isBeadHit) showExitOverlay = true
                            }

                            val stringCenterX = screenWidth * 0.88f
                            // String canvas occupies screenHeight*0.9, centered at screenHeight*0.5
                            val stringTopY = screenHeight * 0.05f
                            val stringHeightPx = screenHeight * 0.9f

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break

                                val absX = change.position.x + screenWidth * 0.5f
                                val absY = change.position.y
                                thumbPosition = Offset(absX, absY)

                                // String physics always follows thumb
                                val targetX = (absX - stringCenterX).coerceIn(-120f, 120f)
                                val targetYFraction = (absY / screenHeight).coerceIn(0.1f, 0.9f)
                                scope.launch { controlXAnim.animateTo(targetX, stringSpring) }
                                scope.launch { controlYAnim.animateTo(targetYFraction, stringSpring) }

                                if (isBeadHit && !hasCounted) {
                                    isBeadDragActive = true
                                    // Map thumb Y → bead progress (0.0=top/counted, 1.0=bottom/uncounted)
                                    val rawProgress = (absY - stringTopY) / stringHeightPx
                                    val progress = rawProgress.coerceIn(0f, 1f)
                                    scope.launch { activeBeadProgress.snapTo(progress) }

                                    // Trigger count when bead dragged to top 30% of string
                                    if (progress < 0.3f) {
                                        hasCounted = true
                                        holdJob.cancel()
                                        scope.launch {
                                            // Fly bead to counted position, then trigger count
                                            activeBeadProgress.animateTo(
                                                0f,
                                                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessHigh)
                                            )
                                            countUp()
                                            activeBeadProgress.snapTo(1f)
                                            isBeadDragActive = false
                                        }
                                    }
                                } else if (!isBeadHit) {
                                    // Non-bead zone: exit gesture only
                                    if (showExitOverlay && (absY - startY) > 240f) {
                                        holdJob.cancel()
                                        showExitOverlay = false
                                        thumbPosition = null
                                        scope.launch { controlXAnim.animateTo(0f, stringSpring) }
                                        scope.launch { controlYAnim.animateTo(0.5f, stringSpring) }
                                        onExit()
                                        return@awaitEachGesture
                                    }
                                }

                                if (change.positionChanged()) change.consume()
                            }

                            // --- Finger lifted ---
                            holdJob.cancel()
                            thumbPosition = null
                            showExitOverlay = false

                            if (isBeadDragActive && !hasCounted) {
                                // Spring bead back to bottom pool
                                scope.launch {
                                    activeBeadProgress.animateTo(
                                        1f,
                                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                                    )
                                    isBeadDragActive = false
                                }
                            }

                            scope.launch { controlXAnim.animateTo(0f, stringSpring) }
                            scope.launch { controlYAnim.animateTo(0.5f, stringSpring) }
                        }
                    }
            )
        }

        // --- Exit overlay pill ---
        AnimatedVisibility(
            visible = showExitOverlay,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Hold to exit",
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}
