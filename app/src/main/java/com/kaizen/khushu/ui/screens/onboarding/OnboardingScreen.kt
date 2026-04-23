package com.kaizen.khushu.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.theme.colorSeeds
import kotlinx.coroutines.launch
import kotlin.math.hypot
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsViewModel.settings.collectAsState()

    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    var isRevealing by remember { mutableStateOf(false) }
    val revealRadius = remember { Animatable(0f) }
    val seedColor = colorSeeds[settings.colorSeed] ?: MaterialTheme.colorScheme.primary

    LaunchedEffect(isRevealing) {
        if (isRevealing) {
            revealRadius.animateTo(
                targetValue = 1f,
                animationSpec = tween(700, easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f))
            )
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Soft radial glow based on user's color choice
        Box(modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(seedColor.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 1.5f
                    )
                )
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 2
        ) { page ->
            // Calculate absolute distance from current view (0 is active, 1 is next, -1 is prev)
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            when (page) {
                0 -> PageIdentity(pageOffset)
                1 -> PageEcosystem(pageOffset)
                2 -> PageCommandCenter(settingsViewModel, pageOffset)
            }
        }

        // Custom Dynamic Pager & Action Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Animated Progress Pill
            val progress = (pagerState.currentPage + pagerState.currentPageOffsetFraction) / 2f
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(64.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (progress + 0.33f).coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(seedColor)
                )
            }

            // Morphing Next/Begin Button
            // Morphing Next/Begin Button
            FloatingActionButton(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        isRevealing = true
                    }
                },
                containerColor = seedColor,
                contentColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInRoot()
                        buttonCenter = Offset(
                            x = position.x + (coordinates.size.width / 2f),
                            y = position.y + (coordinates.size.height / 2f)
                        )
                    }
            ) {
                if (pagerState.currentPage == 2) {
                    Text("Begin", modifier = Modifier.padding(horizontal = 24.dp), fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            }
        }

        // Circular Reveal Transition
        if (isRevealing || revealRadius.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxDistance = hypot(size.width.toDouble(), size.height.toDouble()).toFloat()
                drawCircle(color = seedColor, radius = revealRadius.value * maxDistance, center = buttonCenter)
            }
        }
    }
}

@Composable
private fun PageIdentity(offset: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // The 3D element moves slower than the text (Parallax Depth)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 120.dp)
                .graphicsLayer {
                    translationX = offset * 200f
                    scaleX = 1f - (kotlin.math.abs(offset) * 0.2f)
                    scaleY = scaleX
                }
                .size(300.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp, end = 32.dp, bottom = 64.dp)
                .graphicsLayer { translationX = offset * -400f } // Text moves fast
        ) {
            Text(
                text = "KHUSHU",
                fontFamily = Antonio,
                fontSize = 84.sp,
                lineHeight = 84.sp,
                color = MaterialTheme.colorScheme.onBackground,
                // Use offset for static DP shifting instead of graphicsLayer
                modifier = Modifier.offset(x = (-8).dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A minimalist sanctuary for your daily devotion. Engineered for total focus.",
                fontFamily = BeVietnamPro,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PageEcosystem(offset: Float) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).padding(top = 48.dp)
    ) {
        Text(
            text = "The Ecosystem",
            fontFamily = Antonio,
            fontSize = 42.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer { translationX = offset * 200f }
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Bento Grid with Staggered Parallax
        Row(modifier = Modifier.height(220.dp).fillMaxWidth()) {
            // Box 1: Salah
            BentoCard(
                title = "Salah Canvas",
                subtitle = "Immersive tracking",
                modifier = Modifier.weight(1f).fillMaxHeight().graphicsLayer { translationX = offset * 300f }
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Box 2: Quran
            BentoCard(
                title = "Quran",
                subtitle = "Authentic text",
                modifier = Modifier.weight(0.8f).fillMaxHeight().graphicsLayer { translationX = offset * 450f }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Box 3: Tasbeeh Engine
        BentoCard(
            title = "Tasbeeh Engine",
            subtitle = "Haptic precision & OLED stealth",
            modifier = Modifier.fillMaxWidth().height(140.dp).graphicsLayer { translationX = offset * 600f }
        )
    }
}

@Composable
private fun BentoCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(title, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontFamily = BeVietnamPro, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PageCommandCenter(viewModel: SettingsViewModel, offset: Float) {
    val settings by viewModel.settings.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).padding(top = 48.dp).graphicsLayer { translationX = offset * 250f }
    ) {
        Text("Your Sanctuary", fontFamily = Antonio, fontSize = 42.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(48.dp))

        Text("Aesthetics", fontFamily = BeVietnamPro, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            items(colorSeeds.toList()) { (key, color) ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (settings.colorSeed == key) 4.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                        .clickable { viewModel.setColorSeed(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Segmented Theme Toggle
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(8.dp)
        ) {
            listOf("Light", "Dark", "System").forEach { mode ->
                val isSelected = settings.themeMode == mode
                Surface(
                    onClick = { viewModel.setThemeMode(mode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Text(mode, modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Surface(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp) // Clear the bottom bar
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Enable Reminders", fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}