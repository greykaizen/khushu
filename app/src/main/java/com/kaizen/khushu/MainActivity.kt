package com.kaizen.khushu

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.kaizen.khushu.data.SettingsRepository
import com.kaizen.khushu.data.TasbeehDatabase
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.components.PillNavBar
import com.kaizen.khushu.ui.navigation.*
import com.kaizen.khushu.ui.screens.learn.LearnScreen
import com.kaizen.khushu.ui.screens.learn.LearnSectionDetailScreen
import com.kaizen.khushu.ui.screens.salah.SalahImmersiveScreen
import com.kaizen.khushu.ui.screens.salah.SalahPickerScreen
import com.kaizen.khushu.ui.screens.salah.SalahPreset
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehScreen
import com.kaizen.khushu.data.CanvasDatabase
import com.kaizen.khushu.ui.screens.salah.SalahCanvasScreen
import com.kaizen.khushu.ui.screens.salah.SalahCanvasViewModel
import com.kaizen.khushu.ui.screens.settings.*
import com.kaizen.khushu.ui.screens.tasbeeh.CreateCollectionSheet
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehImmersiveScreen
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehViewModel
import com.kaizen.khushu.data.TasbeehCollection
import com.kaizen.khushu.ui.theme.KhushuTheme
import com.kaizen.khushu.ui.theme.ThemeTransitionProvider

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tasbeehViewModel: TasbeehViewModel
    private lateinit var salahCanvasViewModel: SalahCanvasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)
        settingsViewModel = SettingsViewModel(settingsRepository)
        
        val dao = TasbeehDatabase.getInstance(applicationContext).tasbeehDao()
        tasbeehViewModel = ViewModelProvider(this as ViewModelStoreOwner, TasbeehViewModel.factory(dao))[TasbeehViewModel::class.java]

        val canvasDao = CanvasDatabase.getInstance(applicationContext).canvasDao()
        salahCanvasViewModel = ViewModelProvider(this as ViewModelStoreOwner, SalahCanvasViewModel.factory(canvasDao))[SalahCanvasViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val darkTheme = when (settings.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Handle Status Bar / Insets logic globally here or compute it for KhushuApp
            val view = LocalView.current
            if (!view.isInEditMode) {
                val window = (view.context as Activity).window
                val insetsController = WindowCompat.getInsetsController(window, view)
                
                DisposableEffect(darkTheme) {
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    onDispose {}
                }
            }

            // Handle Keep Screen Awake
            DisposableEffect(settings.keepScreenAwake) {
                if (settings.keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {}
            }

            ThemeTransitionProvider {
                KhushuTheme(
                    darkTheme = darkTheme,
                    dynamicColor = settings.dynamicColor,
                    pureBlack = settings.pureBlack
                ) {
                    KhushuApp(
                        settingsViewModel = settingsViewModel,
                        tasbeehViewModel = tasbeehViewModel,
                        salahCanvasViewModel = salahCanvasViewModel,
                        darkTheme = darkTheme
                    )
                }
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
    tasbeehViewModel: TasbeehViewModel,
    salahCanvasViewModel: SalahCanvasViewModel,
    darkTheme: Boolean
) {
    var immersiveRakats by remember { mutableStateOf<Int?>(null) }
    var immersivePreset by remember { mutableStateOf<SalahPreset>(SalahPreset.Minimal) }
    var activeTasbeehCollection by remember { mutableStateOf<TasbeehCollection?>(null) }
    var showCanvasEditor by remember { mutableStateOf(false) }
    var canvasEditorRakats by remember { mutableIntStateOf(4) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val navController = rememberNavController()


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
                            onStartSalah = { rakats, preset ->
                                immersiveRakats = rakats
                                immersivePreset = preset
                            },
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
                            onCollectionTap = { collection -> activeTasbeehCollection = collection },
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
                        BackHandler {
                            navController.popBackStack()
                            showSettingsSheet = true
                        }
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
                        BackHandler {
                            navController.popBackStack()
                            showSettingsSheet = true
                        }
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
                            viewModel = settingsViewModel,
                            onCustomizeLayout = { rakats ->
                                canvasEditorRakats = rakats
                                showCanvasEditor = true
                            },
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
                            viewModel = settingsViewModel,
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
                preset = immersivePreset,
                salahCanvasViewModel = salahCanvasViewModel,
                onComplete = { immersiveRakats = null },
                onExit = { immersiveRakats = null },
            )
        }

        activeTasbeehCollection?.let { collection ->
            TasbeehImmersiveScreen(
                collection = collection,
                onComplete = { activeTasbeehCollection = null },
                onExit = { activeTasbeehCollection = null },
            )
        }

        if (showCanvasEditor) {
            SalahCanvasScreen(
                targetRakats = canvasEditorRakats,
                viewModel = salahCanvasViewModel,
                onSave = { showCanvasEditor = false },
                onExit = { showCanvasEditor = false }
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
    val initialRoute = initialState.destination.route ?: ""
    val targetRoute = targetState.destination.route ?: ""
    
    val i = AppDestinations.entries.indexOfFirst { it.route == initialRoute }
    val t = AppDestinations.entries.indexOfFirst { it.route == targetRoute }
    
    return when {
        i < 0 || t < 0 -> fadeIn(tween(210, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))
        t > i -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition {
    val initialRoute = initialState.destination.route ?: ""
    val targetRoute = targetState.destination.route ?: ""
    
    val i = AppDestinations.entries.indexOfFirst { it.route == initialRoute }
    val t = AppDestinations.entries.indexOfFirst { it.route == targetRoute }
    
    return when {
        i < 0 || t < 0 -> fadeOut(tween(90)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300))
        t > i -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
}
