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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.model.TasbeehCollection
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Must match BEAD_RADIUS in TasbihWidget.kt
private const val BEAD_RADIUS_DP = 18f

@Composable
fun TasbeehImmersiveScreen(
    viewModel: TasbeehViewModel,
    canvasViewModel: TasbeehCanvasViewModel,
    collection: TasbeehCollection,
    onExit: () -> Unit,
    beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    val layout by canvasViewModel.layout.collectAsStateWithLifecycle()

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
    val activeBeadProgress = remember { Animatable(1f) }
    var isBeadDragActive by remember { mutableStateOf(false) }

    // --- Gesture / interaction state ---
    var thumbPosition by remember { mutableStateOf<Offset?>(null) }
    var showExitOverlay by remember { mutableStateOf(false) }

    fun countUp() {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        currentCount++
        
        // --- String Twang Effect ---
        scope.launch {
            controlXAnim.animateTo(15f, spring(stiffness = Spring.StiffnessHigh))
            controlXAnim.animateTo(-10f, spring(stiffness = Spring.StiffnessHigh))
            controlXAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        }

        if (currentCount >= currentTarget) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (currentItemIndex < items.lastIndex) {
                currentItemIndex++
                currentCount = 0
            }
        }
    }

    fun countDown() {
        if (currentCount > 0) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentCount--
        } else if (currentItemIndex > 0) {
            currentItemIndex--
            currentCount = items[currentItemIndex].targetCount - 1
        }
    }

    // Listen for increment signals
    LaunchedEffect(viewModel.countIncrementSignal) {
        viewModel.countIncrementSignal.collect {
            countUp()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(layout.backgroundColorInt.toLong()))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.VolumeUp -> {
                            countUp()
                            true
                        }
                        Key.VolumeDown -> {
                            countDown()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        // --- Widget rendering ---
        if (screenWidth > 0f && screenHeight > 0f) {
            layout.widgets.sortedBy { it.zIndex }.forEach { widget ->
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

        // --- Interaction zones ---
        if (screenWidth > 0f && screenHeight > 0f) {
            // We use the full screen for gestures now, but hit-test specifically against the string widget's horizontal position
            val stringWidget = layout.widgets.filterIsInstance<TasbihWidget.StringBeadWidget>().firstOrNull()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenWidth, screenHeight, layout.widgets) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()

                            // --- Hit-test: is touch near the string? ---
                            val beadRadiusPx = BEAD_RADIUS_DP * density
                            val stringScreenX = (stringWidget?.offsetX ?: 0.88f) * screenWidth
                            val stringBottomScreenY = screenHeight * 0.95f - beadRadiusPx

                            val touchScreenX = down.position.x
                            val touchScreenY = down.position.y

                            val dx = touchScreenX - stringScreenX
                            val dy = touchScreenY - stringBottomScreenY
                            
                            // Hit test radius: wider horizontally (80dp) to make it easier to grab the string
                            val isBeadHit = kotlin.math.abs(dx) < 60f * density && kotlin.math.abs(dy) < 80f * density

                            val startY = down.position.y
                            var hasCounted = false

                            val holdJob = scope.launch {
                                kotlinx.coroutines.delay(350)
                                if (!isBeadHit) showExitOverlay = true
                            }

                            val stringCenterX = stringScreenX
                            val stringTopY = screenHeight * 0.05f
                            val stringHeightPx = screenHeight * 0.9f

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break

                                val absX = change.position.x
                                val absY = change.position.y
                                thumbPosition = Offset(absX, absY)

                                // String physics follows thumb
                                val targetX = (absX - stringCenterX).coerceIn(-120f, 120f)
                                val targetYFraction = (absY / screenHeight).coerceIn(0.1f, 0.9f)
                                scope.launch { controlXAnim.animateTo(targetX, stringSpring) }
                                scope.launch { controlYAnim.animateTo(targetYFraction, stringSpring) }

                                if (isBeadHit && !hasCounted) {
                                    isBeadDragActive = true
                                    val rawProgress = (absY - stringTopY) / stringHeightPx
                                    val progress = rawProgress.coerceIn(0f, 1f)
                                    scope.launch { activeBeadProgress.snapTo(progress) }

                                    if (progress < 0.3f) {
                                        hasCounted = true
                                        holdJob.cancel()
                                        scope.launch {
                                            activeBeadProgress.animateTo(0f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessHigh))
                                            countUp()
                                            activeBeadProgress.snapTo(1f)
                                            isBeadDragActive = false
                                        }
                                    }
                                } else if (!isBeadHit) {
                                    if (showExitOverlay && (absY - startY) > 240f) {
                                        holdJob.cancel()
                                        showExitOverlay = false
                                        onExit()
                                        return@awaitEachGesture
                                    }
                                }
                                if (change.positionChanged()) change.consume()
                            }

                            holdJob.cancel()
                            thumbPosition = null
                            showExitOverlay = false
                            if (isBeadDragActive && !hasCounted) {
                                scope.launch {
                                    activeBeadProgress.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
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
