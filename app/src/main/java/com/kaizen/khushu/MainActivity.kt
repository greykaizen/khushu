package com.kaizen.khushu

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.kaizen.khushu.data.local.CanvasDatabase
import com.kaizen.khushu.data.model.CanvasPreset
import com.kaizen.khushu.data.repository.AudioRepository
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.data.model.TasbeehCollection
import com.kaizen.khushu.data.model.DhikrItem
import com.kaizen.khushu.data.local.TasbeehDatabase
import com.kaizen.khushu.ui.components.PillNavBar
import com.kaizen.khushu.ui.navigation.*
import com.kaizen.khushu.ui.screens.learn.LearnScreen
import com.kaizen.khushu.ui.screens.learn.LearnSectionDetailScreen
import com.kaizen.khushu.ui.screens.learn.LearnAudioViewModel
import com.kaizen.khushu.ui.screens.learn.LearnReadingScreen
import com.kaizen.khushu.ui.screens.salah.SalahCanvasScreen
import com.kaizen.khushu.ui.screens.salah.SalahCanvasViewModel
import com.kaizen.khushu.ui.screens.salah.SalahImmersiveScreen
import com.kaizen.khushu.ui.screens.salah.SalahPickerScreen
import com.kaizen.khushu.ui.screens.settings.*
import com.kaizen.khushu.ui.screens.tasbeeh.*
import com.kaizen.khushu.ui.theme.KhushuTheme
import com.kaizen.khushu.ui.theme.ThemeTransitionProvider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tasbeehViewModel: TasbeehViewModel
    private lateinit var salahCanvasViewModel: SalahCanvasViewModel
    private lateinit var tasbeehCanvasViewModel: TasbeehCanvasViewModel
    private lateinit var learnAudioViewModel: LearnAudioViewModel
    private lateinit var quranViewModel: com.kaizen.khushu.ui.screens.quran.QuranViewModel
    private lateinit var hadithViewModel: com.kaizen.khushu.ui.screens.hadith.HadithViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)
        settingsViewModel = SettingsViewModel(settingsRepository, applicationContext)

        val dao = TasbeehDatabase.getInstance(applicationContext).tasbeehDao()
        tasbeehViewModel =
            ViewModelProvider(this as ViewModelStoreOwner, TasbeehViewModel.factory(dao))[
                TasbeehViewModel::class.java]

        val canvasDao = CanvasDatabase.getInstance(applicationContext).canvasDao()
        salahCanvasViewModel =
            ViewModelProvider(
                this as ViewModelStoreOwner,
                SalahCanvasViewModel.factory(canvasDao)
            )[SalahCanvasViewModel::class.java]

        tasbeehCanvasViewModel =
            ViewModelProvider(
                this as ViewModelStoreOwner,
                TasbeehCanvasViewModel.factory(canvasDao)
            )[TasbeehCanvasViewModel::class.java]

        val audioRepository = AudioRepository(applicationContext)
        learnAudioViewModel =
            ViewModelProvider(
                this as ViewModelStoreOwner,
                LearnAudioViewModel.factory(audioRepository, applicationContext)
            )[LearnAudioViewModel::class.java]

        quranViewModel = ViewModelProvider(this)[com.kaizen.khushu.ui.screens.quran.QuranViewModel::class.java]
        hadithViewModel = ViewModelProvider(this)[com.kaizen.khushu.ui.screens.hadith.HadithViewModel::class.java]

        setContent {
            val isLoaded by settingsViewModel.isSettingsLoaded.collectAsState()

            if (!isLoaded) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                return@setContent
            }

            val settings by settingsViewModel.settings.collectAsState()
            val darkTheme =
                when (settings.themeMode) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }

            val view = LocalView.current
            if (!view.isInEditMode) {
                val window = (view.context as Activity).window
                val insetsController = WindowCompat.getInsetsController(window, view)

                DisposableEffect(darkTheme) {
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    onDispose {}
                }
            }

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
                    pureBlack = settings.pureBlack,
                    colorSeed = settings.colorSeed
                ) {
                    KhushuApp(
                        settingsViewModel = settingsViewModel,
                        tasbeehViewModel = tasbeehViewModel,
                        salahCanvasViewModel = salahCanvasViewModel,
                        tasbeehCanvasViewModel = tasbeehCanvasViewModel,
                        learnAudioViewModel = learnAudioViewModel,
                        quranViewModel = quranViewModel,
                        hadithViewModel = hadithViewModel,
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsViewModel.settings.value
        if (settings.volumeCounting &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                    keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        ) {
            tasbeehViewModel.incrementActiveCount()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KhushuApp(
    settingsViewModel: SettingsViewModel,
    tasbeehViewModel: TasbeehViewModel,
    salahCanvasViewModel: SalahCanvasViewModel,
    tasbeehCanvasViewModel: TasbeehCanvasViewModel,
    learnAudioViewModel: LearnAudioViewModel,
    quranViewModel: com.kaizen.khushu.ui.screens.quran.QuranViewModel,
    hadithViewModel: com.kaizen.khushu.ui.screens.hadith.HadithViewModel,
    darkTheme: Boolean
) {
    var immersiveRakats by remember { mutableStateOf<Int?>(null) }
    var immersivePresetId by remember { mutableStateOf("signature") }
    var activeTasbeehCollection by remember { mutableStateOf<TasbeehCollection?>(null) }
    var showBeadCustomizer by remember { mutableStateOf(false) }
    var showTasbihCanvasEditor by remember { mutableStateOf(false) }
    var showCanvasEditor by remember { mutableStateOf(false) }
    var canvasEditorRakats by remember { mutableIntStateOf(4) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val settings by settingsViewModel.settings.collectAsState()

    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val pillClearance = navBarBottomDp + 30.dp + 56.dp
    val fabBottomPadding = navBarBottomDp + 30.dp + 56.dp + 20.dp
    val topClearance = with(density) { WindowInsets.statusBars.getTop(density).toDp() } + 64.dp

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hazeState = remember { HazeState() }
    val showNavBar = AppDestinations.entries.any { it.route == currentRoute }
    val currentDestination = AppDestinations.fromRoute(currentRoute) ?: AppDestinations.SALAH

    val startRoute = remember {
        val saved = settingsViewModel.settings.value.startupTab
        AppDestinations.fromRoute(saved)?.route ?: AppDestinations.SALAH.route
    }

    val onNavigateTab: (AppDestinations) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(startRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startRoute,
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
                            onStartSalah = { rakats, presetId ->
                                immersiveRakats = rakats
                                immersivePresetId = presetId ?: "current"
                            },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState,
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
                            settingsViewModel = settingsViewModel,
                            onCollectionTap = { collection ->
                                activeTasbeehCollection = collection
                            },
                            onEditCollection = { showCreateSheet = true },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState,
                            contentPadding =
                                PaddingValues(
                                    start = 0.dp,
                                    top = topClearance,
                                    end = 0.dp,
                                    bottom = pillClearance
                                )
                        )
                    }

                    composable(
                        route = AppDestinations.LEARN.route,
                        enterTransition = { tabEnter() },
                        exitTransition = {
                            if (targetState.destination.route?.startsWith("learn_detail") == true)
                                scaleOut(targetScale = 0.85f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) +
                                        fadeOut(animationSpec = spring(stiffness = 800f))
                            else tabExit()
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = spring(stiffness = 800f)) +
                                    scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                        },
                        popExitTransition = { tabExit() },
                    ) {
                        LearnScreen(
                            onSectionTap = { title ->
                                navController.navigate("learn_detail/$title")
                            },
                            onCardTap = { topicId ->
                                if (topicId == "quran_browser") {
                                    navController.navigate("quran")
                                } else if (topicId.startsWith("quran_surah_")) {
                                    val surahId = topicId.removePrefix("quran_surah_")
                                    navController.navigate("quran/$surahId")
                                } else if (topicId.startsWith("hadith_book_")) {
                                    val bookId = topicId.removePrefix("hadith_book_")
                                    navController.navigate("hadith/$bookId")
                                } else {
                                    navController.navigate("learn_card/$topicId")
                                }
                            },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState,
                            contentPadding = PaddingValues(top = topClearance, bottom = pillClearance),
                            settingsViewModel = settingsViewModel
                        )
                    }

                    composable(
                        route = "quran",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                    ) {
                        com.kaizen.khushu.ui.screens.quran.QuranSurahListScreen(
                            onSurahTap = { num -> navController.navigate("quran/$num") },
                            viewModel = quranViewModel
                        )
                    }

                    composable(
                        route = "quran/{surahNumber}",
                        arguments = listOf(navArgument("surahNumber") { type = NavType.IntType }),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                    ) { backStackEntry ->
                        val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                        com.kaizen.khushu.ui.screens.quran.QuranReaderScreen(
                            surahNumber = surahNumber,
                            onBack = { navController.popBackStack() },
                            viewModel = quranViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }

                    composable(
                        route = "hadith/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                        val book = com.kaizen.khushu.data.model.BUNDLED_HADITH_BOOKS.find { it.id == bookId }
                        if (book != null) {
                            com.kaizen.khushu.ui.screens.hadith.HadithSectionListScreen(
                                book = book,
                                onSectionTap = { num, title ->
                                    navController.navigate("hadith/$bookId/$num/$title")
                                },
                                onBack = { navController.popBackStack() },
                                viewModel = hadithViewModel
                            )
                        }
                    }

                    composable(
                        route = "hadith/{bookId}/{sectionNum}/{sectionTitle}",
                        arguments = listOf(
                            navArgument("bookId") { type = NavType.StringType },
                            navArgument("sectionNum") { type = NavType.IntType },
                            navArgument("sectionTitle") { type = NavType.StringType }
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = 0.8f, stiffness = 400f)) },
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                        val sectionNum = backStackEntry.arguments?.getInt("sectionNum") ?: 0
                        val sectionTitle = backStackEntry.arguments?.getString("sectionTitle") ?: ""
                        val book = com.kaizen.khushu.data.model.BUNDLED_HADITH_BOOKS.find { it.id == bookId }

                        com.kaizen.khushu.ui.screens.hadith.HadithReaderScreen(
                            bookId = bookId,
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                            bookName = book?.name ?: "",
                            onBack = { navController.popBackStack() },
                            viewModel = hadithViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }

                    composable(
                        route = LEARN_DETAIL_ROUTE,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) { backStackEntry ->
                        val sectionTitle = backStackEntry.arguments?.getString("sectionTitle") ?: ""
                        LearnSectionDetailScreen(
                            sectionTitle = sectionTitle,
                            onBack = { navController.popBackStack() },
                            onCardTap = { topicId ->
                                if (topicId.startsWith("quran_surah_")) {
                                    val surahId = topicId.removePrefix("quran_surah_")
                                    navController.navigate("quran/$surahId")
                                } else if (topicId.startsWith("hadith_book_")) {
                                    val bookId = topicId.removePrefix("hadith_book_")
                                    navController.navigate("hadith/$bookId")
                                } else {
                                    navController.navigate("learn_card/$topicId")
                                }
                            },
                        )
                    }

                    composable(
                        route = "learn_card/{topicId}?ayah={ayah}",
                        arguments = listOf(
                            navArgument("topicId") { type = NavType.StringType },
                            navArgument("ayah") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        ),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) { backStackEntry ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
                        val ayahIndex = backStackEntry.arguments?.getString("ayah")?.toIntOrNull()
                        val topic = com.kaizen.khushu.data.repository.LearnRepository.getSections(context)
                            .flatMap { it.topics }.find { it.id == topicId }
                        if (topic != null) {
                            LearnReadingScreen(
                                topic = topic,
                                settingsViewModel = settingsViewModel,
                                learnAudioViewModel = learnAudioViewModel,
                                onBack = { navController.popBackStack() },
                                initialAyahIndex = ayahIndex
                            )
                        }
                    }

                    composable(
                        route = SETTINGS_ROUTE,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) {
                        BackHandler {
                            navController.popBackStack()
                            showSettingsSheet = true
                        }
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateCounter = {
                                navController.navigate(SETTINGS_COUNTER_ROUTE)
                            },
                            onNavigateAppearance = {
                                navController.navigate(SETTINGS_APPEARANCE_ROUTE)
                            },
                            onBack = {
                                navController.popBackStack()
                                showSettingsSheet = true
                            }
                        )
                    }

                    composable(
                        route = SETTINGS_COUNTER_ROUTE,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
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
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
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
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) {
                        BackHandler {
                            navController.popBackStack()
                            showSettingsSheet = true
                        }
                        CustomizeScreen(
                            onNavigateBranding = {
                                navController.navigate(CUSTOMIZE_BRANDING_ROUTE)
                            },
                            onNavigateSalah = { navController.navigate(CUSTOMIZE_SALAH_ROUTE) },
                            onNavigateTasbeeh = {
                                navController.navigate(CUSTOMIZE_TASBEEH_ROUTE)
                            },
                            onBack = {
                                navController.popBackStack()
                                showSettingsSheet = true
                            }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_SALAH_ROUTE,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
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
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) {
                        TasbeehCustomizeScreen(
                            viewModel = settingsViewModel,
                            onPreview = { showTasbihCanvasEditor = true },
                            onCustomizeBeads = { showBeadCustomizer = true },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_BRANDING_ROUTE,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            )
                        },
                    ) {
                        BrandingSettingsScreen(
                            settingsViewModel = settingsViewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showNavBar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 30.dp),
        ) {
            PillNavBar(
                currentDestination = currentDestination,
                onDestinationSelected = onNavigateTab,
                hazeState = hazeState,
            )
        }

        AnimatedVisibility(
            visible = currentRoute == AppDestinations.TASBEEH.route,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = fabBottomPadding + 16.dp
                    ),
        ) {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialShapes.Square.toShape(),
                elevation = FloatingActionButtonDefaults.elevation(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Tasbih",
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        immersiveRakats?.let { rakats ->
            val activeLayout by salahCanvasViewModel.layout.collectAsStateWithLifecycle()

            val currentCanvasPreset =
                CanvasPreset(
                    id = "current",
                    name = "Current Canvas",
                    backgroundColor = activeLayout.backgroundColorInt,
                    widgets = activeLayout.widgets,
                    isDeletable = false
                )

            var finalPresetToRender = currentCanvasPreset

            if (immersivePresetId != "current") {
                val dbPreset by
                salahCanvasViewModel
                    .getPresetFlow(immersivePresetId)
                    .collectAsStateWithLifecycle(initialValue = null)
                if (dbPreset != null) {
                    finalPresetToRender = dbPreset!!
                }
            }

            SalahImmersiveScreen(
                targetRakats = rakats,
                preset = finalPresetToRender,
                showExitButton = settings.showExitButton,
                showCompletionText = settings.showCompletionText,
                completionText = settings.completionText.ifBlank { "الحمد لله" },
                onComplete = { immersiveRakats = null },
                onExit = { immersiveRakats = null }
            )
        }

        activeTasbeehCollection?.let { collection ->
            val beadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER
            TasbeehImmersiveScreen(
                viewModel = tasbeehViewModel,
                canvasViewModel = tasbeehCanvasViewModel,
                collection = collection,
                beadStyle = beadStyle,
                onExit = { activeTasbeehCollection = null },
            )
        }

        if (showTasbihCanvasEditor) {
            TasbeehCanvasScreen(
                viewModel = tasbeehCanvasViewModel,
                onExit = { showTasbihCanvasEditor = false }
            )
        }

        if (showBeadCustomizer) {
            val beadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER
            TasbihBeadCustomizerSheet(
                currentStyle = beadStyle,
                onStyleSelected = { style ->
                    settingsViewModel.setTasbihBeadStyle(style.name)
                    showBeadCustomizer = false
                },
                onDismiss = { showBeadCustomizer = false },
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
            settingsViewModel = settingsViewModel,
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

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition =
    fadeIn(animationSpec = spring(stiffness = 800f)) +
            scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition =
    fadeOut(animationSpec = spring(stiffness = 800f))
