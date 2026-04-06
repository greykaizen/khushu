package com.kaizen.khushu.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.UserSettings
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateGeneral: () -> Unit,
    onNavigateCounter: () -> Unit,
    onNavigateAppearance: () -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(horizontal = 12.dp, vertical = 32.dp),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Settings",
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
                title = "General",
                detail = "Screen, Awake & System Settings",
                iconRes = com.kaizen.khushu.R.drawable.ic_salah,
                onClick = onNavigateGeneral
            )

            MenuSectionItem(
                title = "Counter",
                detail = "Haptics, Volume Keys & Feedback",
                iconRes = com.kaizen.khushu.R.drawable.ic_tasbeeh,
                onClick = onNavigateCounter
            )

            MenuSectionItem(
                title = "Appearance",
                detail = "Theme, Dynamic Color & AMOLED",
                iconRes = com.kaizen.khushu.R.drawable.ic_learn,
                onClick = onNavigateAppearance
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
