package com.kaizen.khushu

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.kaizen.khushu.data.TasbeehDatabase
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.components.PillNavBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import com.kaizen.khushu.ui.screens.salah.SalahImmersiveScreen
import com.kaizen.khushu.ui.screens.salah.SalahPickerScreen
import com.kaizen.khushu.ui.screens.salah.SalahPreset
import com.kaizen.khushu.ui.screens.settings.SettingsSheet
import com.kaizen.khushu.ui.screens.learn.LearnScreen
import com.kaizen.khushu.ui.screens.tasbeeh.CreateCollectionSheet
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehScreen
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehViewModel
import com.kaizen.khushu.ui.theme.KhushuTheme
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhushuTheme {
                KhushuApp()
            }
        }
    }
}

@Composable
private fun KhushuApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SALAH) }
    var immersiveRakats by rememberSaveable { mutableStateOf<Int?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val context = LocalContext.current
    val dao = remember {
        TasbeehDatabase.getInstance(context.applicationContext).tasbeehDao()
    }
    val tasbeehViewModel: TasbeehViewModel = viewModel(factory = TasbeehViewModel.factory(dao))

    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val pillClearance = navBarBottomDp + 30.dp + 56.dp
    val fabBottomPadding = navBarBottomDp + 30.dp + 56.dp + 20.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            KhushuAppBar(
                title = currentDestination.label,
                onSettingsClick = { showSettingsSheet = true },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                when (currentDestination) {
                    AppDestinations.SALAH -> SalahPickerScreen(
                        onStartPrayer = { immersiveRakats = it },
                        navBarClearance = pillClearance,
                    )
                    AppDestinations.TASBEEH -> TasbeehScreen(
                        viewModel = tasbeehViewModel,
                        onCollectionTap = { /* TODO: immersive counter Phase 4 */ },
                        contentPadding = PaddingValues(bottom = pillClearance),
                    )
                    AppDestinations.LEARN -> LearnScreen(
                        contentPadding = PaddingValues(bottom = pillClearance),
                    )
                }
            }
        }

        PillNavBar(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 30.dp),
        )

        // FAB — visible only on Tasbeeh tab, animates in/out with scale+fade
        AnimatedVisibility(
            visible = currentDestination == AppDestinations.TASBEEH,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = fabBottomPadding),
        ) {
            IconButton(
                onClick = { showCreateSheet = true },
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Tasbih",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        immersiveRakats?.let { rakats ->
            SalahImmersiveScreen(
                targetRakats = rakats,
                preset = SalahPreset.Minimal,
                onComplete = { immersiveRakats = null },
                onExit = { immersiveRakats = null },
            )
        }
    }

    if (showCreateSheet) {
        CreateCollectionSheet(
            viewModel = tasbeehViewModel,
            onDismiss = { showCreateSheet = false },
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(onDismiss = { showSettingsSheet = false })
    }
}
