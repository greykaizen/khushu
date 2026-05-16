package com.kaizen.khushu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.navigation.AppDestinations
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeTint
import kotlin.math.roundToInt

private val PillShape = RoundedCornerShape(50)

@Composable
fun PillNavBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val tabPositions = remember { mutableStateMapOf<AppDestinations, Float>() }
        var rowCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

        val indicatorTargetX by animateFloatAsState(
            targetValue = tabPositions[currentDestination] ?: 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "indicatorX",
        )

        Box(
            modifier = modifier
                .clip(PillShape)
                .background(Color.Black.copy(alpha = 0.2f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = PillShape,
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            blurRadius = 20.dp,
                            tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))),
                        )
                    )
            )

            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.onGloballyPositioned { rowCoords = it },
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppDestinations.entries.forEach { destination ->
                        PillNavItem(
                            destination = destination,
                            isSelected = destination == currentDestination,
                            onClick = { onDestinationSelected(destination) },
                            onPositioned = { coords ->
                                val row = rowCoords ?: return@PillNavItem
                                val centerX = row.localPositionOf(
                                    sourceCoordinates = coords,
                                    relativeToSource = Offset(x = coords.size.width / 2f, y = 0f),
                                ).x
                                tabPositions[destination] = centerX
                            },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset {
                            IntOffset(
                                x = (indicatorTargetX - 12.5.dp.toPx()).roundToInt(),
                                y = 0,
                            )
                        }
                        .padding(bottom = 2.dp)
                        .width(25.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                )
            }
        }
    }
}

@Composable
private fun PillNavItem(
    destination: AppDestinations,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .onGloballyPositioned(onPositioned)
            .clip(PillShape)
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) // selected pill bar opacity
                else Color.Transparent,
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = 24.dp, vertical = 14.dp), // Fixed horizontal size as requested
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                if (targetState) {
                    (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                } else {
                    (slideInVertically { -it } + fadeIn()) togetherWith
                            (slideOutVertically { it } + fadeOut())
                }
            },
            label = "navItemContent",
        ) { selected ->
            if (selected) {
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Icon(
                    painter = painterResource(id = destination.icon),
                    contentDescription = destination.label,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
