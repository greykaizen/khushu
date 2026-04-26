package com.kaizen.khushu.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaizen.khushu.R
import com.kaizen.khushu.ui.util.rememberMorphShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KhushuLogoBadge(
    logoStyle: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    iconSize: Dp = 48.dp,
) {
    val context = LocalContext.current
    val bgColor = when (logoStyle) {
        "DARK"  -> Color(0xFF000000)
        "LIGHT" -> Color(0xFFFFFFFF)
        "GREEN" -> Color(0xFF1B7A3E)
        else    -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                Color(ContextCompat.getColor(context, android.R.color.system_accent1_500))
            } else {
                Color(0xFF27847D)
            }
        }
    }
    val iconTint = when (logoStyle) {
        "LIGHT" -> Color(0xFF000000)
        else    -> Color(0xFFFFFFFF)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 1. Cumulative rotation — adds 90 deg on every click
    var targetRotation by remember { mutableStateOf(0f) }
    
    // 2. Spring-based animations for "Natural" feel
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logo_scale"
    )

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "logo_morph_progress"
    )

    val morphShape = rememberMorphShape(
        start = MaterialShapes.Cookie12Sided,
        end = MaterialShapes.Sunny,
        progress = pressProgress
    )

    Surface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { targetRotation += 90f }
            ),
        shape = morphShape,
        color = bgColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.ic_khushu_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        // Counter-rotate the icon so it stays upright
                        rotationZ = -rotation
                    },
                tint = iconTint,
            )
        }
    }
}

