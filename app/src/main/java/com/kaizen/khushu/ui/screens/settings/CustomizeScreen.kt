package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    onNavigateBranding: () -> Unit,
    onNavigateSalah: () -> Unit,
    onNavigateTasbeeh: () -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { SettingsTopBarTitle("Customize", scrollBehavior) },
                navigationIcon = { SettingsBackButton(onBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            MenuSectionItem(
                title = "Branding",
                detail = "App logo style & identity",
                imageVector = Icons.Default.Palette,
                onClick = onNavigateBranding,
            )

            MenuSectionItem(
                title = "Salah Screen",
                detail = "Transitions, Layouts & Timers",
                iconRes = com.kaizen.khushu.R.drawable.ic_salah,
                onClick = onNavigateSalah
            )

            MenuSectionItem(
                title = "Tasbeeh Screen",
                detail = "Haptics, Pulse & Animation",
                iconRes = com.kaizen.khushu.R.drawable.ic_tasbeeh,
                onClick = onNavigateTasbeeh
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
