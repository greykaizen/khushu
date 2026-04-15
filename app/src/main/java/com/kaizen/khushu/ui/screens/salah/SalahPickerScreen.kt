package com.kaizen.khushu.ui.screens.salah

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun SalahPickerScreen(
    onStartSalah: (rakats: Int, presetId: String?) -> Unit,
    onSettingsClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    var selectedRakat by rememberSaveable { mutableIntStateOf(2) }

    Box(modifier = modifier.fillMaxSize()) {
        // Full-screen tap to start
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .navigationBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onStartSalah(selectedRakat, "current") },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.offset(y = -12.dp),
            ) {
                RakatPicker(
                    selectedRakat = selectedRakat,
                    onRakatSelected = { selectedRakat = it },
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Tap on the screen to Start",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }

        KhushuAppBar(
            title = AppDestinations.SALAH.label,
            onSettingsClick = onSettingsClick,
//            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.TopCenter),
        )

    }
}
