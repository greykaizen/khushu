package com.kaizen.khushu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.R
import androidx.compose.ui.geometry.Size  // NOT android.util.Size

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KhushuAppBar(
    title: String,
    onSettingsClick: () -> Unit,
    onBookmarksClick: (() -> Unit)? = null,
    showLogoTitle: Boolean = false,
    titleContent: (@Composable () -> Unit)? = null,
    centerOverlayContent: (@Composable () -> Unit)? = null,
    startContent: (@Composable () -> Unit)? = null,
    isListMode: Boolean? = null,
    onGridClick: (() -> Unit)? = null,
    onListClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val expressiveShape = MaterialShapes.Circle.toShape()

    // Simple Row layout when startContent is provided — no TopAppBar, no overlay hacks
    if (startContent != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            startContent()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(expressiveShape)
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
        }
        return
    }

    val hideDefaultTitle = titleContent == null && title.isBlank() && !showLogoTitle
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
            )
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .wrapContentWidth()
                            .offset(x = if (titleContent == null) (-8).dp else 0.dp),
                        horizontalAlignment = if (titleContent != null) Alignment.Start else Alignment.CenterHorizontally,
                    ) {
                        if (titleContent != null) {
                            titleContent()
                        } else if (!hideDefaultTitle) {
                            AnimatedContent(
                                targetState = showLogoTitle,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                            fadeOut(animationSpec = tween(90))
                                },
                                label = "AppBarTitleTransition"
                            ) { isLogoTitle ->
                                if (isLogoTitle) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_khushu_logo),
                                        contentDescription = "Khushu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(64.dp)
                                    )
                                } else {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                        if (titleContent == null && !hideDefaultTitle) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            )
                        }
                    }
                },
                actions = {
                    if (isListMode != null && onGridClick != null && onListClick != null) {
                        AppBarIconButton(
                            iconRes = R.drawable.ic_toggle,
                            contentDescription = "Grid view",
                            selected = !isListMode,
                            onClick = onGridClick,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AppBarIconButton(
                            iconRes = R.drawable.ic_list_view,
                            contentDescription = "List view",
                            selected = isListMode,
                            onClick = onListClick,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (onBookmarksClick != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(expressiveShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable(
                                    onClick = onBookmarksClick,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = "Bookmarks",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(expressiveShape)
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

            if (centerOverlayContent != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 12.dp, start = 20.dp, end = 68.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    centerOverlayContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppBarIconButton(
    iconRes: Int,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val expressiveShape = MaterialShapes.Cookie9Sided.toShape()
    
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }
    val iconTint = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(expressiveShape)
            .background(containerColor)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
    }
}
