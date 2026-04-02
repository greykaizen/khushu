package com.kaizen.khushu.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.R
import com.kaizen.khushu.ui.theme.Antonio
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.BuildConfig

private enum class SettingsView {
    Main, History
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: SettingsViewModel,
    onNavigateSettings: () -> Unit,
    onNavigateCustomize: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentView by remember { mutableStateOf(SettingsView.Main) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        val sheetHeightModifier = if (currentView == SettingsView.History) {
            Modifier.fillMaxHeight(0.92f)
        } else {
            Modifier.fillMaxHeight(0.65f)
        }

        val contentPadding = if (currentView == SettingsView.History) {
            PaddingValues(0.dp)
        } else {
            PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(sheetHeightModifier)
                .padding(contentPadding)
        ) {
            AnimatedContent(
                targetState = currentView,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "settings_sheet_nav",
                modifier = Modifier.weight(1f)
            ) { view ->
                when (view) {
                    SettingsView.Main -> MainMenuView(
                        onSettings = onNavigateSettings,
                        onCustomize = onNavigateCustomize,
                        onHistory = { currentView = SettingsView.History }
                    )
                    SettingsView.History -> HistoryView(
                        onBack = { currentView = SettingsView.Main }
                    )
                }
            }

            // Fixed Footer (Social + Version) — Hidden in History
            if (currentView == SettingsView.Main) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SocialCircleButton(R.drawable.ic_github)
                        Spacer(modifier = Modifier.width(16.dp))
                        SocialCircleButton(R.drawable.ic_telegram)
                        Spacer(modifier = Modifier.width(16.dp))
                        SocialCircleButton(R.drawable.ic_email)
                    }
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 50.dp, top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuView(
    onSettings: () -> Unit,
    onCustomize: () -> Unit,
    onHistory: () -> Unit
) {
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.End,
    ) {
        SettingsMenuItem(text = "Settings", onClick = onSettings)
        SettingsMenuItem(text = "Customize", onClick = onCustomize)
        SettingsMenuItem(text = "History", onClick = onHistory)
    }
}

@Composable
private fun SettingsMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = Antonio,
            fontSize = 34.sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.End
    )
}

@Composable
private fun HistoryView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(18.dp))
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = BeVietnamPro, fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "History tracking coming soon to Khushu.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}


@Composable
private fun SocialCircleButton(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { /* Handle link */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(35.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
