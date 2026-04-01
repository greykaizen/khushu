package com.kaizen.khushu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhushuAppBar(
    title: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 35.dp,
                            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f)
                        )
                    )
                } else Modifier
            )
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .offset(x = (-8).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                    fadeOut(animationSpec = tween(90))
                        },
                        label = "AppBarTitleTransition"
                    ) { targetTitle ->
                        Text(
                            text = targetTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(
                            onClick = onSettingsClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.padding(top = 12.dp, start = 20.dp, end = 20.dp),
        )
    }
}
