package com.kaizen.khushu.ui.screens.tasbeeh

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.model.TasbeehCollection
import com.kaizen.khushu.data.repository.UserSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun TasbeehImmersiveScreen(
    viewModel: TasbeehViewModel,
    canvasViewModel: TasbeehCanvasViewModel,
    collection: TasbeehCollection,
    settings: UserSettings,
    onExit: () -> Unit,
    beadStyle: BeadStyle = BeadStyle.CLASSIC_AMBER,
    customBeadStyle: com.kaizen.khushu.data.model.CustomBeadStyle? = null,
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val layout by canvasViewModel.layout.collectAsStateWithLifecycle()
    val soundPlayer = remember { TasbihSoundPlayer(context) }

    BackHandler(onBack = onExit)

    DisposableEffect(Unit) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            soundPlayer.release()
        }
    }

    var currentItemIndex by remember { mutableIntStateOf(0) }
    var currentCount by remember { mutableIntStateOf(0) }
    val items = collection.items
    val currentItem = items.getOrNull(currentItemIndex)
    val currentTarget = currentItem?.targetCount ?: 33

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    val scope = rememberCoroutineScope()

    // Finger Y on the string canvas — drives the live fisheye wave.
    // Written directly from pointer events; read at Canvas draw-phase via the provider lambda.
    var fingerYOnString by remember { mutableStateOf<Float?>(null) }
    // fisheyeAlpha fades 0→1 on touch-down and 1→0 on lift — smooth fisheye release.
    val fisheyeAlpha = remember { Animatable(0f) }
    // Transit bead state — tracks a bead being dragged across the gap between stacks.
    val transitYAnim      = remember { Animatable(0f) }
    val transitLiftAnim   = remember { Animatable(0f) }
    var isInTransit       by remember { mutableStateOf(false) }
    var transitFromBottom by remember { mutableStateOf(true) }
    // Conveyor-belt scroll: snapped to ±beadStep then animated to 0 on each successful transit.
    val scrollOffsetAnim  = remember { Animatable(0f) }
    var widgetsVisible by remember { mutableStateOf(true) }
    var resetProgress by remember { mutableFloatStateOf(0f) }
    var resetArmed by remember { mutableStateOf(false) }
    var showResetOverlay by remember { mutableStateOf(false) }

    fun playTasbihCollisionSound() {
        if (settings.tasbihSoundEnabled) {
            soundPlayer.play(settings.tasbihSoundId)
        }
    }

    fun registerIncrement(playSound: Boolean = true) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        currentCount++
        if (playSound) {
            playTasbihCollisionSound()
        }
        if (currentCount >= currentTarget) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (currentItemIndex < items.lastIndex) {
                currentItemIndex++
                currentCount = 0
            }
        }
    }

    fun registerDecrement() {
        if (currentCount > 0 || currentItemIndex > 0) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (currentCount > 0) currentCount--
            else { currentItemIndex--; currentCount = items[currentItemIndex].targetCount - 1 }
        }
    }

    LaunchedEffect(viewModel.countIncrementSignal) {
        viewModel.countIncrementSignal.collect { registerIncrement() }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(layout.backgroundColorInt.toLong()))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (settings.tasbeehVolumeEnabled && event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.VolumeUp -> { registerIncrement(); true }
                        Key.VolumeDown -> { registerDecrement(); true }
                        else -> false
                    }
                } else false
            }
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        if (screenWidth > 0f && screenHeight > 0f) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                AnimatedVisibility(visible = widgetsVisible, enter = fadeIn(), exit = fadeOut()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        layout.widgets.sortedBy { it.zIndex }.forEach { widget ->
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    translationX = (widget.offsetX * screenWidth) - (size.width / 2f)
                                    translationY = (widget.offsetY * screenHeight) - (size.height / 2f)
                                    scaleX = widget.scale
                                    scaleY = widget.scale
                                    transformOrigin = TransformOrigin.Center
                                    clip = false
                                }
                            ) {
                                TasbihWidgetRenderer(
                                    widget = widget,
                                    currentCount = currentCount,
                                    currentItem = currentItem,
                                    stringControlXOffset = 0f,
                                    stringControlYFraction = 0.5f,
                                    countedBeads = currentCount,
                                    totalBeads = currentTarget,
                                    beadStyle = beadStyle,
                                    customBeadStyle = customBeadStyle,
                                    thumbPositionProvider = if (widget is TasbihWidget.StringBeadWidget) {
                                        val offsetY = widget.offsetY
                                        { fingerYOnString?.let { fy -> Offset(0f, fy - offsetY * screenHeight) } }
                                    } else { { null } },
                                    fisheyeStrengthProvider = if (widget is TasbihWidget.StringBeadWidget) {
                                        { fisheyeAlpha.value }
                                    } else { { 1f } },
                                    transitBeadProvider = if (widget is TasbihWidget.StringBeadWidget) {
                                        val offsetY  = widget.offsetY
                                        val canvasH  = screenHeight * 0.9f
                                        { if (isInTransit) transitYAnim.value - offsetY * screenHeight + canvasH / 2f else null }
                                    } else { { null } },
                                    transitLiftProvider = if (widget is TasbihWidget.StringBeadWidget) {
                                        { transitLiftAnim.value }
                                    } else { { 0f } },
                                    transitFromBottom = transitFromBottom,
                                    scrollOffsetProvider = if (widget is TasbihWidget.StringBeadWidget) {
                                        { scrollOffsetAnim.value }
                                    } else { { 0f } },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (screenWidth > 0f && screenHeight > 0f) {
            val stringWidget = layout.widgets.filterIsInstance<TasbihWidget.StringBeadWidget>().firstOrNull()
            val hasString = stringWidget != null

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenWidth, screenHeight, layout.widgets, widgetsVisible, settings.tasbeehStealthModeAllowed) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()

                            val startScreenX = down.position.x
                            val startScreenY = down.position.y
                            val stringScreenX = (stringWidget?.offsetX ?: 0.88f) * screenWidth
                            val isNearString = hasString && widgetsVisible &&
                                kotlin.math.abs(startScreenX - stringScreenX) < 70f * density && !showResetOverlay

                            var localResetArmed = false
                            var hasMoved = false

                            // Hold-to-reset: only fires when not near the string.
                            val holdJob = if (!isNearString) {
                                scope.launch {
                                    kotlinx.coroutines.delay(500)
                                    showResetOverlay = true
                                    resetProgress = 0f; resetArmed = false
                                    for (i in 1..20) { kotlinx.coroutines.delay(40); resetProgress = i / 20f }
                                    resetArmed = true; localResetArmed = true
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            } else null

                            // Snap fisheye strength to full on touch-down near string.
                            if (isNearString) scope.launch { fisheyeAlpha.snapTo(1f) }

                            // ── Boundary-bead positions in screen space ──────────────
                            // These are natural (no-zoom) positions; close enough for trigger detection.
                            val sw = stringWidget
                            val beadR    = BEAD_RADIUS_BASE * (sw?.beadSizeScale ?: 1f) * density
                            val beadGap  = beadR * 0.4f
                            val canvasH  = screenHeight * 0.9f
                            val canvasTopScreen = (sw?.offsetY ?: 0.5f) * screenHeight - canvasH / 2f
                            val topCount    = minOf(currentCount,          sw?.topStackLimit    ?: 0).coerceAtLeast(0)
                            val bottomCount = minOf(currentTarget - currentCount, sw?.bottomStackLimit ?: 0).coerceAtLeast(0)

                            // Bottom-most bead of the TOP stack (screen Y)
                            val topBoundaryY = canvasTopScreen + beadR +
                                (topCount - 1).coerceAtLeast(0) * (2f * beadR + beadGap)
                            // Top-most bead of the BOTTOM stack (screen Y)
                            val bottomBoundaryY = canvasTopScreen + canvasH - beadR -
                                (bottomCount - 1).coerceAtLeast(0) * (2f * beadR + beadGap)
                            val gapDistance = (bottomBoundaryY - topBoundaryY).coerceAtLeast(0f)
                            // Commit once the bead has travelled a reasonable portion of the gap
                            // from its source side, so upward and downward swipes feel balanced.
                            val commitDistance = gapDistance * 0.25f
                            val triggerZone = beadR * 2.5f  // how close the finger must start
                            val triggerMove = beadR * 1.5f  // how far it must move to begin transit

                            val isNearTopBoundary    = topCount    > 0 && kotlin.math.abs(startScreenY - topBoundaryY)    < triggerZone
                            val isNearBottomBoundary = bottomCount > 0 && kotlin.math.abs(startScreenY - bottomBoundaryY) < triggerZone

                            // Track finger Y and handle transit activation.
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                val dy = change.position.y - startScreenY
                                if (isNearString) fingerYOnString = change.position.y
                                if (kotlin.math.abs(dy) > 20f) hasMoved = true

                                // Check proximity to boundary bead using CURRENT finger position
                                // (not startScreenY) so swiping through the stack naturally triggers transit.
                                if (isNearString && !isInTransit) {
                                    val curY = change.position.y
                                    val currentlyNearBottom = bottomCount > 0 && kotlin.math.abs(curY - bottomBoundaryY) < triggerZone
                                    val currentlyNearTop    = topCount    > 0 && kotlin.math.abs(curY - topBoundaryY)    < triggerZone
                                    val canGoUp   = dy < -triggerMove && currentlyNearBottom
                                    val canGoDown = dy >  triggerMove && currentlyNearTop
                                    if (canGoUp || canGoDown) {
                                        isInTransit    = true
                                        transitFromBottom = canGoUp
                                        scope.launch {
                                            transitLiftAnim.snapTo(1f)
                                            transitYAnim.snapTo(change.position.y)
                                        }
                                    }
                                }
                                if (isInTransit) scope.launch { transitYAnim.snapTo(change.position.y) }
                                change.consume()
                            }

                            holdJob?.cancel()

                            // Fisheye fade-out: keep fingerYOnString set while strength fades to 0,
                            // then null it. Beads return to natural size smoothly.
                            if (isNearString) {
                                scope.launch {
                                    fisheyeAlpha.animateTo(0f, tween(200))
                                    fingerYOnString = null
                                }
                            } else {
                                fingerYOnString = null
                            }

                            // ── Transit resolution ───────────────────────────────────
                            if (isInTransit) {
                                val currentY = transitYAnim.value
                                val committed = if (transitFromBottom) {
                                    currentY < (bottomBoundaryY - commitDistance)
                                } else {
                                    currentY > (topBoundaryY + commitDistance)
                                }
                                if (committed) {
                                    // Animate transit bead to landing, register count, then slide stacks.
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val destY = if (transitFromBottom)
                                        topBoundaryY + 2f * beadR + beadGap
                                    else
                                        bottomBoundaryY - 2f * beadR - beadGap
                                    scope.launch {
                                        val moveJob = launch {
                                            if (transitFromBottom) {
                                                val impactY = destY - (beadR * 0.22f)
                                                transitYAnim.animateTo(
                                                    impactY,
                                                    tween(
                                                        durationMillis = 135,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                )
                                                playTasbihCollisionSound()
                                                transitYAnim.animateTo(
                                                    destY,
                                                    tween(durationMillis = 70)
                                                )
                                            } else {
                                                transitYAnim.animateTo(destY, spring(stiffness = 500f, dampingRatio = 0.85f))
                                            }
                                        }
                                        val liftJob = launch {
                                            transitLiftAnim.animateTo(0f, tween(180))
                                        }
                                        moveJob.join()
                                        liftJob.join()
                                        // Displace stacks to new post-count positions, then slide into place.
                                        val beadStep = beadR * 2.4f  // 2r + 0.4r gap in canvas pixels
                                        val slideDir = if (transitFromBottom) beadStep else -beadStep
                                        scrollOffsetAnim.snapTo(slideDir)
                                        if (transitFromBottom) registerIncrement(playSound = false) else registerDecrement()
                                        isInTransit = false
                                        scrollOffsetAnim.animateTo(0f, spring(stiffness = 220f, dampingRatio = 0.88f))
                                    }
                                } else {
                                    // Didn't cross midpoint → spring back to source position.
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val snapTarget = if (transitFromBottom) bottomBoundaryY else topBoundaryY
                                    scope.launch {
                                        val moveJob = launch {
                                            transitYAnim.animateTo(snapTarget, spring(stiffness = 300f, dampingRatio = 0.7f))
                                        }
                                        val liftJob = launch {
                                            transitLiftAnim.animateTo(0f, tween(180))
                                        }
                                        moveJob.join()
                                        liftJob.join()
                                        isInTransit = false
                                    }
                                }
                            } else if (!hasMoved) {
                                when {
                                    localResetArmed -> {
                                        currentCount = 0
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    showResetOverlay -> {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    // No string or stealth hidden: single tap counts, double-tap toggles stealth.
                                    !hasString || !widgetsVisible -> {
                                        if (settings.tasbeehStealthModeAllowed) {
                                            val secondTap = withTimeoutOrNull(300L) { awaitFirstDown(requireUnconsumed = false) }
                                            if (secondTap != null) {
                                                secondTap.consume()
                                                widgetsVisible = !widgetsVisible
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                registerIncrement()
                                            }
                                        } else {
                                            registerIncrement()
                                        }
                                    }
                                    // String visible, not near string: double-tap → stealth toggle.
                                    settings.tasbeehStealthModeAllowed && !isNearString -> {
                                        val secondTap = withTimeoutOrNull(300L) { awaitFirstDown(requireUnconsumed = false) }
                                        if (secondTap != null) {
                                            secondTap.consume()
                                            widgetsVisible = !widgetsVisible
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                    // Tap off-string with no stealth allowed → increment.
                                    !isNearString -> { registerIncrement() }
                                    // Tap on string with no move → do nothing (Step 3 handles swipe).
                                }
                            }

                            showResetOverlay = false
                        }
                    }
            )
        }

        AnimatedVisibility(
            visible = showResetOverlay,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
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
            visible = !showResetOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 48.dp)
        ) {
            OutlinedButton(
                onClick = onExit,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(text = "Exit", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
