package com.kaizen.khushu.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahCustomizeScreen(
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp, vertical = 32.dp),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Salah Visuals",
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
            SectionHeader("Interface")
            Text(
                "Customize transitions, timer visibility, and immersive layout options for the Salah tracker.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(32.dp))
            // TODO: Add specific toggles like "Show Step Timer", "Fluid Transitions"
        }
    }
}
