package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    onNavigateBranding: () -> Unit,
    onNavigatePalette: () -> Unit,
    onNavigateSalah: () -> Unit,
    onNavigateTasbeeh: () -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp, vertical = 32.dp),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Customize",
                        fontFamily = BeVietnamPro,
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 17.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            MenuSectionItem(
                title = "Salah Screen",
                detail = "Transitions, Layouts & Timers",
                iconRes = com.kaizen.khushu.R.drawable.ic_salah,
                onClick = onNavigateSalah
            )

            MenuSectionItem(
                title = "Tasbih Screen",
                detail = "Fonts, Pulse & Animation",
                iconRes = com.kaizen.khushu.R.drawable.ic_tasbeeh,
                onClick = onNavigateTasbeeh
            )

            MenuSectionItem(
                title = "Branding",
                detail = "Logos, Socials & Identity",
                iconRes = com.kaizen.khushu.R.drawable.ic_learn, // Placeholder icon
                onClick = onNavigateBranding
            )

            MenuSectionItem(
                title = "Palette",
                detail = "Colors, Fonts & Themes",
                iconRes = com.kaizen.khushu.R.drawable.ic_learn,
                onClick = onNavigatePalette
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
