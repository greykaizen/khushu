package com.kaizen.khushu.ui.screens.salah

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SalahPickerScreen(
    onStartPrayer: (rakats: Int) -> Unit,
    navBarClearance: Dp = 88.dp,
    modifier: Modifier = Modifier,
) {
    var selectedRakat by rememberSaveable { mutableIntStateOf(2) }

    // Full-screen tap to start — picker scroll (drag) consumes its own events,
    // so quick taps anywhere (including over the picker) reach this handler.
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = navBarClearance)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onStartPrayer(selectedRakat) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-35).dp),  // ← adjust this value
        ) {
            RakatPicker(
                selectedRakat = selectedRakat,
                onRakatSelected = { selectedRakat = it },
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Tap on the screen to Start",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}
