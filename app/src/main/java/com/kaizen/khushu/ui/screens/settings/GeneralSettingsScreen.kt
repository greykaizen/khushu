package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp, vertical = 32.dp),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "General",
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
            SectionHeader("Screen")
            ToggleItem(
                title = "Keep Screen Awake",
                subtitle = "Prevents device sleep during sessions",
                checked = settings.keepScreenAwake,
                onCheckedChange = { viewModel.toggleKeepScreenAwake(it) }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
