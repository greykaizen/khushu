package com.kaizen.khushu

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
//import androidx.compose.animation.slideIntoContainer
//import androidx.compose.animation.slideOutOfContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.kaizen.khushu.data.TasbeehDatabase
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.components.PillNavBar
import com.kaizen.khushu.ui.navigation.*
import com.kaizen.khushu.ui.screens.learn.LearnScreen
import com.kaizen.khushu.ui.screens.learn.LearnSectionDetailScreen
import com.kaizen.khushu.ui.screens.salah.SalahImmersiveScreen
import com.kaizen.khushu.ui.screens.salah.SalahPickerScreen
import com.kaizen.khushu.ui.screens.salah.SalahPreset
import com.kaizen.khushu.ui.screens.settings.*
import com.kaizen.khushu.ui.screens.tasbeeh.CreateCollectionSheet
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehScreen
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehViewModel
import com.kaizen.khushu.ui.theme.KhushuTheme
import androidx.compose.ui.platform.LocalContext
import com.kaizen.khushu.data.SettingsRepository
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tasbeehViewModel: TasbeehViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)
        settingsViewModel = SettingsViewModel(settingsRepository)
        
        val dao = TasbeehDatabase.getInstance(applicationContext).tasbeehDao()
        tasbeehViewModel = ViewModelProvider(this as ViewModelStoreOwner, TasbeehViewModel.factory(dao))[TasbeehViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            // Handle Keep Screen Awake
            DisposableEffect(settings.keepScreenAwake) {
                if (settings.keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {}
            }

            KhushuTheme(
                dynamicColor = settings.dynamicColor,
                pureBlack = settings.pureBlack
            ) {
                KhushuApp(
                    settingsViewModel = settingsViewModel,
                    tasbeehViewModel = tasbeehViewModel
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsViewModel.settings.value
        if (settings.volumeCounting && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            tasbeehViewModel.incrementActiveCount()
            return true // Consume the event to prevent volume UI from showing
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
private fun KhushuApp(
    settingsViewModel: SettingsViewModel,
    tasbeehViewModel: TasbeehViewModel
) {
    var immersiveRakats by rememberSaveable { mutableStateOf<Int?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val navController = rememberNavController()

    val darkTheme = isSystemInDarkTheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        
        DisposableEffect(darkTheme) {
            insetsController.isAppearanceLightStatusBars = !darkTheme
            onDispose {}
        }
    }

    // ViewModel removed from here as it's passed in from MainActivity

    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val pillClearance = navBarBottomDp + 30.dp + 56.dp
    val fabBottomPadding = navBarBottomDp + 30.dp + 56.dp + 20.dp
    val topClearance = with(density) { WindowInsets.statusBars.getTop(density).toDp() } + 64.dp

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val onNavigateTab: (AppDestinations) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(AppDestinations.SALAH.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.SALAH.route,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(
                        route = AppDestinations.SALAH.route,
                        enterTransition = { tabEnter() },
                        exitTransition = { tabExit() },
                        popEnterTransition = { tabEnter() },
                        popExitTransition = { tabExit() },
                    ) {
                        SalahPickerScreen(
                            onStartPrayer = { immersiveRakats = it },
                            onSettingsClick = { showSettingsSheet = true },
                            onNavigateTab = onNavigateTab,
                            navBarClearance = 0.dp, // Center visually on screen, not pushed up
                        )
                    }

                    composable(
                        route = AppDestinations.TASBEEH.route,
                        enterTransition = { tabEnter() },
                        exitTransition = { tabExit() },
                        popEnterTransition = { tabEnter() },
                        popExitTransition = { tabExit() },
                    ) {
                        TasbeehScreen(
                            viewModel = tasbeehViewModel,
                            onCollectionTap = { /* TODO: immersive counter Phase 4 */ },
                            onSettingsClick = { showSettingsSheet = true },
                            onNavigateTab = onNavigateTab,
                            contentPadding = PaddingValues(start = 0.dp, top = topClearance, end = 0.dp, bottom = pillClearance),
                        )
                    }

                    composable(
                        route = AppDestinations.LEARN.route,
                        enterTransition = { tabEnter() },
                        exitTransition = {
                            if (targetState.destination.route?.startsWith("learn_detail") == true)
                                scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(tween(90))
                            else tabExit()
                        },
                        popEnterTransition = {
                            fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))
                        },
                        popExitTransition = { tabExit() },
                    ) {
                        LearnScreen(
                            onSectionTap = { title ->
                                navController.navigate("learn_detail/$title")
                            },
                            onSettingsClick = { showSettingsSheet = true },
                            onNavigateTab = onNavigateTab,
                            contentPadding = PaddingValues(start = 0.dp, top = topClearance, end = 0.dp, bottom = pillClearance),
                        )
                    }

                    composable(
                        route = LEARN_DETAIL_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) { backStackEntry ->
                        val sectionTitle = backStackEntry.arguments?.getString("sectionTitle") ?: ""
                        LearnSectionDetailScreen(
                            sectionTitle = sectionTitle,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = SETTINGS_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        SettingsScreen(
                            onNavigateGeneral = { navController.navigate(SETTINGS_GENERAL_ROUTE) },
                            onNavigateCounter = { navController.navigate(SETTINGS_COUNTER_ROUTE) },
                            onNavigateAppearance = { navController.navigate(SETTINGS_APPEARANCE_ROUTE) },
                            onBack = {
                                navController.popBackStack()
                                showSettingsSheet = true
                            }
                        )
                    }

                    composable(
                        route = SETTINGS_GENERAL_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        GeneralSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = SETTINGS_COUNTER_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        CounterSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = SETTINGS_APPEARANCE_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        AppearanceSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        CustomizeScreen(
                            onNavigateBranding = { navController.navigate(CUSTOMIZE_BRANDING_ROUTE) },
                            onNavigatePalette = { navController.navigate(CUSTOMIZE_PALETTE_ROUTE) },
                            onNavigateSalah = { navController.navigate(CUSTOMIZE_SALAH_ROUTE) },
                            onNavigateTasbeeh = { navController.navigate(CUSTOMIZE_TASBEEH_ROUTE) },
                            onBack = {
                                navController.popBackStack()
                                showSettingsSheet = true
                            }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_SALAH_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        SalahCustomizeScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_TASBEEH_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        TasbeehCustomizeScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_BRANDING_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        BrandingSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_PALETTE_ROUTE,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        },
                    ) {
                        PaletteSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        // FAB — visible only on Tasbeeh tab, animates in/out with scale+fade
        AnimatedVisibility(
            visible = currentRoute == AppDestinations.TASBEEH.route,
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
        SettingsSheet(
            viewModel = settingsViewModel,
            onNavigateSettings = {
                showSettingsSheet = false
                navController.navigate(SETTINGS_ROUTE)
            },
            onNavigateCustomize = {
                showSettingsSheet = false
                navController.navigate(CUSTOMIZE_ROUTE)
            },
            onDismiss = { showSettingsSheet = false }
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition {
    val i = AppDestinations.entries.indexOfFirst { it.route == initialState.destination.route }
    val t = AppDestinations.entries.indexOfFirst { it.route == targetState.destination.route }
    return when {
        i < 0 || t < 0 -> fadeIn(tween(210, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))
        t > i -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition {
    val i = AppDestinations.entries.indexOfFirst { it.route == initialState.destination.route }
    val t = AppDestinations.entries.indexOfFirst { it.route == targetState.destination.route }
    return when {
        i < 0 || t < 0 -> fadeOut(tween(90)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300))
        t > i -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
}
