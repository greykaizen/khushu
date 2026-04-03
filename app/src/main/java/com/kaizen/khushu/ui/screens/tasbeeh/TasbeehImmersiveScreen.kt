package com.kaizen.khushu.ui.screens.tasbeeh

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.data.TasbeehCollection
import com.kaizen.khushu.ui.theme.Antonio
import kotlinx.coroutines.delay

private val SWIPE_THRESHOLD = 80.dp

@Composable
fun TasbeehImmersiveScreen(
    collection: TasbeehCollection,
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

    var currentItemIndex by remember { mutableIntStateOf(0) }
    var currentCount by remember { mutableIntStateOf(0) }
    var showOverlay by remember { mutableStateOf(false) }

    val activeItem = collection.items.getOrNull(currentItemIndex)
    val isComplete = activeItem == null

    val density = LocalDensity.current
    val swipeThresholdPx = remember(density) { with(density) { SWIPE_THRESHOLD.toPx() } }
    var swipeAccum by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            delay(3_000)
            onComplete()
        }
    }

    fun onTap() {
        if (showOverlay) {
            showOverlay = false
            return
        }
        if (isComplete) return
        
        currentCount++
        if (currentCount >= activeItem!!.targetCount) {
            currentItemIndex++
            currentCount = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isComplete, showOverlay) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { if (!isComplete) showOverlay = !showOverlay },
                )
            }
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
        // Dhikr list (top section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp)
        ) {
            collection.items.forEachIndexed { index, item ->
                val alpha = when {
                    index < currentItemIndex -> 0.35f   // completed
                    index == currentItemIndex -> 1.0f   // active
                    else -> 0.4f                        // remaining
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .alpha(alpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = if (index == currentItemIndex) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = if (index == currentItemIndex) "$currentCount / ${item.targetCount}" else "${item.targetCount}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Center counter
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Text(
                    text = "Collection Complete",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = currentCount,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.75f) + fadeIn()) togetherWith
                                    (scaleOut(targetScale = 1.15f) + fadeOut())
                        },
                        label = "dhikrCount"
                    ) { displayCount ->
                        Text(
                            text = displayCount.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                            color = Color.White.copy(alpha = 0.9f),
                            fontFamily = Antonio
                        )
                    }
                    Text(
                        text = "out of ${activeItem?.targetCount ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
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
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color(0xFF222222),
                        shape = RoundedCornerShape(50),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(50),
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                currentItemIndex = 0
                                currentCount = 0
                                showOverlay = false
                            }
                        )
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
