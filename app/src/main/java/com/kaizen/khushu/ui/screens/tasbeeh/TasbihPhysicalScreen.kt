package com.kaizen.khushu.ui.screens.tasbeeh

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.data.DefaultTasbihPreset
import com.kaizen.khushu.data.TasbihCanvasPreset
import com.kaizen.khushu.data.TasbihWidget
import com.kaizen.khushu.data.TasbihWidgetRenderer
import com.kaizen.khushu.data.TasbeehCollection
import kotlinx.coroutines.launch

@Composable
fun TasbihPhysicalScreen(
    collection: TasbeehCollection,
    onExit: () -> Unit,
    preset: TasbihCanvasPreset = DefaultTasbihPreset,
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    // Hide system bars — same pattern as TasbeehImmersiveScreen
    DisposableEffect(Unit) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    var currentItemIndex by remember { mutableIntStateOf(0) }
    var currentCount by remember { mutableIntStateOf(0) }
    val currentItem = collection.items.getOrNull(currentItemIndex)

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    // --- Phase 2: Bezier string physics ---
    // controlXAnim: px deflection of the Bezier midpoint from string center (0 = straight)
    // controlYAnim: 0.0–1.0 fraction of string height where the apex sits (0.5 = midpoint)
    val controlXAnim = remember { Animatable(0f) }
    val controlYAnim = remember { Animatable(0.5f) }
    val scope = rememberCoroutineScope()

    // Heavy-string spring: medium damping gives realistic lag + settling wobble
    val stringSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        // Render widgets sorted by zIndex — mirrors SalahImmersiveScreen pattern
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
                    )
                }
            }
        }

        // Right-half drag zone — detects thumb and feeds the string spring.
        // Covers the right 50% so left-side widgets don't intercept gestures.
        if (screenWidth > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .pointerInput(screenWidth, screenHeight) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Snap control point toward initial touch instantly
                                // so the string "snaps to attention" before lagging
                                val stringCenterX = screenWidth * 0.88f
                                val targetX = (offset.x + (screenWidth * 0.5f)) - stringCenterX
                                val targetYFraction = offset.y / screenHeight
                                scope.launch {
                                    controlXAnim.animateTo(targetX * 0.3f, stringSpring)
                                }
                                scope.launch {
                                    controlYAnim.animateTo(targetYFraction, stringSpring)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                // Spring-chase the thumb — calling animateTo repeatedly
                                // interrupts and restarts the spring, creating natural lag
                                val stringCenterX = screenWidth * 0.88f
                                val thumbAbsX = change.position.x + (screenWidth * 0.5f)
                                val targetX = (thumbAbsX - stringCenterX).coerceIn(-120f, 120f)
                                val targetYFraction = (change.position.y / screenHeight).coerceIn(0.1f, 0.9f)
                                scope.launch {
                                    controlXAnim.animateTo(targetX, stringSpring)
                                }
                                scope.launch {
                                    controlYAnim.animateTo(targetYFraction, stringSpring)
                                }
                            },
                            onDragEnd = {
                                // Release: spring settles back to straight center
                                scope.launch { controlXAnim.animateTo(0f, stringSpring) }
                                scope.launch { controlYAnim.animateTo(0.5f, stringSpring) }
                            },
                            onDragCancel = {
                                scope.launch { controlXAnim.animateTo(0f, stringSpring) }
                                scope.launch { controlYAnim.animateTo(0.5f, stringSpring) }
                            }
                        )
                    }
            )
        }

        // Exit — temporary visible button; replaced by gesture overlay in Phase 3
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit",
                tint = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}
