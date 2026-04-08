package com.kaizen.khushu.ui.screens.tasbeeh

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaizen.khushu.data.DefaultTasbihPreset
import com.kaizen.khushu.data.TasbihCanvasPreset
import com.kaizen.khushu.data.TasbihWidgetRenderer
import com.kaizen.khushu.data.TasbeehCollection

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
                    }
                ) {
                    TasbihWidgetRenderer(
                        widget = widget,
                        currentCount = currentCount,
                        currentItem = currentItem,
                    )
                }
            }
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
