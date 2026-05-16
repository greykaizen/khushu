package com.kaizen.khushu

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import android.Manifest
import android.content.ComponentName
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.kaizen.khushu.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.lifecycle.lifecycleScope
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
import com.kaizen.khushu.data.repository.IslamicEventsRepository
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.data.model.TasbeehCollection
import com.kaizen.khushu.data.model.DhikrItem
import com.kaizen.khushu.data.local.TasbeehDatabase
import com.kaizen.khushu.notifications.PrayerNotificationScheduler
import com.kaizen.khushu.notifications.toPrayerNotificationScheduleConfig
import com.kaizen.khushu.ui.components.PillNavBar
import com.kaizen.khushu.ui.components.DeveloperWelcomeDialog
import com.kaizen.khushu.ui.navigation.*
import com.kaizen.khushu.ui.screens.onboarding.OnboardingScreen
import com.kaizen.khushu.ui.screens.home.HomeScreen
import com.kaizen.khushu.ui.screens.home.HomeViewModel
import com.kaizen.khushu.data.repository.PrayerTimeRepository
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
import com.kaizen.khushu.ui.util.add
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val DEBUG_FORCE_ONBOARDING = false // SET TO FALSE FOR RELEASE
    }

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tasbeehViewModel: TasbeehViewModel
    private lateinit var salahCanvasViewModel: SalahCanvasViewModel
    private lateinit var tasbeehCanvasViewModel: TasbeehCanvasViewModel
    private lateinit var learnAudioViewModel: LearnAudioViewModel
    private lateinit var quranViewModel: com.kaizen.khushu.ui.screens.quran.QuranViewModel
    private lateinit var hadithViewModel: com.kaizen.khushu.ui.screens.hadith.HadithViewModel
    private lateinit var prayerTimeRepository: PrayerTimeRepository
    private lateinit var homeViewModel: com.kaizen.khushu.ui.screens.home.HomeViewModel

    // Tracks whether the tasbeeh immersive screen is currently active so that
    // volume key interception only fires there and not app-wide.
    var isOnTasbeehImmersive: Boolean = false

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val media3Controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controllerFuture = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        settingsRepository = SettingsRepository(applicationContext)
        settingsViewModel = SettingsViewModel(settingsRepository, applicationContext)
        prayerTimeRepository = PrayerTimeRepository(settingsRepository)
        val islamicEventsRepository = IslamicEventsRepository(applicationContext)
        val prayerNotificationScheduler = PrayerNotificationScheduler(applicationContext)
        val prayerManager = com.kaizen.khushu.logic.PrayerManager(settingsRepository, prayerTimeRepository)

        homeViewModel = ViewModelProvider(
            this as ViewModelStoreOwner,
            com.kaizen.khushu.ui.screens.home.HomeViewModel.factory(
                settingsRepository,
                prayerTimeRepository,
                islamicEventsRepository,
                prayerManager
            )
        )[com.kaizen.khushu.ui.screens.home.HomeViewModel::class.java]

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

        lifecycleScope.launch {
            settingsRepository.settingsFlow
                .map { settings -> settings.toPrayerNotificationScheduleConfig() to settings }
                .distinctUntilChanged { old, new -> old.first == new.first }
                .collect { (_, settings) ->
                    prayerNotificationScheduler.syncNotifications(settings)
                }
        }

        lifecycleScope.launch {
            while (true) {
                val settings = settingsRepository.settingsFlow.first()
                prayerNotificationScheduler.maybeDeliverCurrentPrayerNotification(settings)
                delay(30_000L)
            }
        }

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
                        homeViewModel = homeViewModel,
                        darkTheme = darkTheme,
                        media3Controller = media3Controller
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsViewModel.settings.value
        // Only intercept volume keys when the tasbeeh immersive screen is active
        // and the user has volume-counting enabled. Everywhere else, pass the
        // event to the OS so the system volume slider works normally.
        if (isOnTasbeehImmersive &&
            settings.volumeCounting &&
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
    homeViewModel: HomeViewModel,
    darkTheme: Boolean,
    media3Controller: MediaController?
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val settings by settingsViewModel.settings.collectAsState()
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val screenContentPadding = systemBarsPadding.add(top = 64.dp, bottom = 86.dp)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hazeState = remember { HazeState() }
    val showNavBar = AppDestinations.entries.any { it.route == currentRoute }

    // Keep MainActivity.isOnTasbeehImmersive in sync with the current route so that
    // volume key interception is scoped to the Tasbih immersive screen only.
    val activity = LocalContext.current as? MainActivity
    SideEffect {
        activity?.isOnTasbeehImmersive = currentRoute?.startsWith("tasbeeh/immersive") == true
    }
    val currentDestination = AppDestinations.fromRoute(currentRoute) ?: AppDestinations.SALAH
    val showDeveloperWelcome = !settings.developerWelcomeDismissed && currentRoute != ONBOARDING_ROUTE

    val startRoute = remember {
        val saved = settingsViewModel.settings.value.startupTab
        AppDestinations.fromRoute(saved)?.route ?: AppDestinations.SALAH.route
    }
    
    val onNavigateTab: (AppDestinations) -> Unit = { dest ->
        if (currentRoute != dest.route) {
            navController.navigate(dest.route) {
                popUpTo(startRoute) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
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
                        route = ONBOARDING_ROUTE,
                        enterTransition = { fadeIn(tween(M3Duration)) },
                        exitTransition = { fadeOut(tween(M3Duration)) },
                    ) {
                        // We will create this screen next
                        OnboardingScreen(
                            settingsViewModel = settingsViewModel,
                            onComplete = {
                                settingsViewModel.setOnboardingCompleted(true)
                                navController.navigate(AppDestinations.SALAH.route) {
                                    popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = AppDestinations.HOME.route,
                        enterTransition = { tabEnter() },
                        exitTransition = { tabExit() },
                        popEnterTransition = { tabEnter() },
                        popExitTransition = { tabExit() },
                    ) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            hazeState = hazeState,
                            contentPadding = screenContentPadding,
                            onSettingsClick = { showSettingsSheet = true },
                            onPrayClick = { navController.navigate(AppDestinations.SALAH.route) }
                        )
                    }

                    composable(
                        route = AppDestinations.SALAH.route,
                        enterTransition = { tabEnter() },
                        exitTransition = { tabExit() },
                        popEnterTransition = { tabEnter() },
                        popExitTransition = { tabExit() },
                    ) {
                        SalahPickerScreen(
                            onStartSalah = { rakats, presetId ->
                                navController.navigate("salah/immersive/$rakats/${presetId ?: "signature"}")
                            },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState
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
                            canvasViewModel = tasbeehCanvasViewModel,
                            onCollectionTap = { collection ->
                                navController.navigate("tasbeeh/immersive/${collection.id}")
                            },
                            onEditCollection = { showCreateSheet = true },
                            onCustomizeCanvas = { navController.navigate("tasbeeh/canvas") },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState,
                            contentPadding = screenContentPadding
                        )
                    }

                    composable(
                        route = AppDestinations.LEARN.route,
                        enterTransition = { tabEnter() },
                        exitTransition = { tabExit() },
                        popEnterTransition = { tabEnter() },
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
                                    val surahPayload = topicId.removePrefix("quran_surah_")
                                    val surahId = surahPayload.substringBefore("?")
                                    val ayahIndex = surahPayload.substringAfter("?ayah=", "").toIntOrNull()
                                    navController.navigate(
                                        if (ayahIndex != null) "quran/$surahId?ayah=$ayahIndex" else "quran/$surahId"
                                    )
                                } else if (topicId.startsWith("hadith_book_")) {
                                    val bookId = topicId.removePrefix("hadith_book_")
                                    navController.navigate("hadith/$bookId")
                                } else {
                                    navController.navigate("learn_card/$topicId")
                                }
                            },
                            onSettingsClick = { showSettingsSheet = true },
                            hazeState = hazeState,
                            contentPadding = screenContentPadding,
                            settingsViewModel = settingsViewModel
                        )
                    }

                    composable(
                        route = "quran",
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        com.kaizen.khushu.ui.screens.quran.QuranSurahListScreen(
                            onSurahTap = { num -> navController.navigate("quran/$num") },
                            viewModel = quranViewModel
                        )
                    }

                    composable(
                        route = "quran/{surahNumber}?ayah={ayah}",
                        arguments = listOf(
                            navArgument("surahNumber") { type = NavType.IntType },
                            navArgument("ayah") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        ),
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) { backStackEntry ->
                        val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                        val initialAyahIndex = backStackEntry.arguments?.getString("ayah")?.toIntOrNull()
                        com.kaizen.khushu.ui.screens.quran.QuranReaderScreen(
                            surahNumber = surahNumber,
                            initialAyahIndex = initialAyahIndex,
                            onBack = { navController.popBackStack() },
                            onNextSurah = { next ->
                                navController.navigate("quran/$next") {
                                    popUpTo("quran/$surahNumber") { inclusive = true }
                                }
                            },
                            viewModel = quranViewModel,
                            settingsViewModel = settingsViewModel,
                            media3Controller = media3Controller
                        )
                    }

                    composable(
                        route = "hadith/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
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
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
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
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) { backStackEntry ->
                        val sectionTitle = backStackEntry.arguments?.getString("sectionTitle") ?: ""
                        LearnSectionDetailScreen(
                            sectionTitle = sectionTitle,
                            onBack = { navController.popBackStack() },
                            onCardTap = { topicId ->
                                if (topicId.startsWith("quran_surah_")) {
                                    val surahPayload = topicId.removePrefix("quran_surah_")
                                    val surahId = surahPayload.substringBefore("?")
                                    val ayahIndex = surahPayload.substringAfter("?ayah=", "").toIntOrNull()
                                    navController.navigate(
                                        if (ayahIndex != null) "quran/$surahId?ayah=$ayahIndex" else "quran/$surahId"
                                    )
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
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
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
                                media3Controller = media3Controller,
                                onBack = { navController.popBackStack() },
                                initialAyahIndex = ayahIndex
                            )
                        }
                    }

                    composable(
                        route = SETTINGS_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateCounter = {
                                navController.navigate(SETTINGS_COUNTER_ROUTE)
                            },
                            onNavigateAppearance = {
                                navController.navigate(SETTINGS_APPEARANCE_ROUTE)
                            },
                            onNavigatePrayer = {
                                navController.navigate(com.kaizen.khushu.ui.navigation.SETTINGS_PRAYER_ROUTE)
                            },
                            onNavigateAbout = {
                                navController.navigate(com.kaizen.khushu.ui.navigation.SETTINGS_ABOUT_ROUTE)
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = SETTINGS_COUNTER_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        CounterSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = SETTINGS_APPEARANCE_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        AppearanceSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = com.kaizen.khushu.ui.navigation.SETTINGS_PRAYER_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        PrayerSettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = com.kaizen.khushu.ui.navigation.SETTINGS_ABOUT_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        AboutSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        CustomizeScreen(
                            settingsViewModel = settingsViewModel,
                            onNavigateSalah = { navController.navigate(CUSTOMIZE_SALAH_ROUTE) },
                            onNavigateTasbeeh = {
                                navController.navigate(CUSTOMIZE_TASBEEH_ROUTE)
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_SALAH_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        SalahCustomizeScreen(
                            viewModel = settingsViewModel,
                            onCustomizeLayout = { rakats ->
                                navController.navigate("salah/canvas/$rakats")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = CUSTOMIZE_TASBEEH_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        TasbeehCustomizeScreen(
                            viewModel = settingsViewModel,
                            onPreview = { navController.navigate(TASBEEH_CANVAS_ROUTE) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // --- NEW IMMERSIVE & EDITOR ROUTES ---

                    composable(
                        route = TASBEEH_IMMERSIVE_ROUTE,
                        arguments = listOf(navArgument("collectionId") { type = NavType.StringType }),
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                        val collections by tasbeehViewModel.collections.collectAsStateWithLifecycle()
                        val collection = collections.find { it.id.toString() == collectionId }
                        if (collection != null) {
                            val beadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER
                            val customBeadStyle = settings.customBeadStyles.find { it.id == settings.activeBeadStyleId }
                            
                            TasbeehImmersiveScreen(
                                viewModel = tasbeehViewModel,
                                canvasViewModel = tasbeehCanvasViewModel,
                                collection = collection,
                                beadStyle = beadStyle,
                                customBeadStyle = customBeadStyle,
                                settings = settings,
                                onExit = { navController.popBackStack() },
                            )
                        }
                    }

                    composable(
                        route = SALAH_IMMERSIVE_ROUTE,
                        arguments = listOf(
                            navArgument("rakats") { type = NavType.IntType },
                            navArgument("presetId") { type = NavType.StringType }
                        ),
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) { backStackEntry ->
                        val rakats = backStackEntry.arguments?.getInt("rakats") ?: 2
                        val presetId = backStackEntry.arguments?.getString("presetId") ?: "signature"
                        
                        val activeLayout by salahCanvasViewModel.layout.collectAsStateWithLifecycle()
                        val currentCanvasPreset = CanvasPreset(
                            id = "current",
                            name = "Current Canvas",
                            backgroundColor = activeLayout.backgroundColorInt,
                            widgets = activeLayout.widgets,
                            isDeletable = false
                        )
                        var finalPresetToRender = currentCanvasPreset
                        if (presetId != "current") {
                            val dbPreset by salahCanvasViewModel.getPresetFlow(presetId).collectAsStateWithLifecycle(initialValue = null)
                            if (dbPreset != null) finalPresetToRender = dbPreset!!
                        }

                        SalahImmersiveScreen(
                            targetRakats = rakats,
                            preset = finalPresetToRender,
                            showExitButton = settings.showExitButton,
                            showCompletionText = settings.showCompletionText,
                            completionText = settings.completionText.ifBlank { "الحمد لله" },
                            onComplete = { navController.popBackStack() },
                            onExit = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = SALAH_CANVAS_ROUTE,
                        arguments = listOf(navArgument("rakats") { type = NavType.IntType }),
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) { backStackEntry ->
                        val rakats = backStackEntry.arguments?.getInt("rakats") ?: 4
                        SalahCanvasScreen(
                            targetRakats = rakats,
                            viewModel = salahCanvasViewModel,
                            onSave = { navController.popBackStack() },
                            onExit = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = TASBEEH_CANVAS_ROUTE,
                        enterTransition = { subScreenEnter() },
                        exitTransition = { subScreenExit() },
                        popEnterTransition = { subScreenPopEnter() },
                        popExitTransition = { subScreenPopExit() },
                    ) {
                        TasbeehCanvasScreen(
                            viewModel = tasbeehCanvasViewModel,
                            settingsViewModel = settingsViewModel,
                            onExit = { navController.popBackStack() }
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
                    .padding(end = 16.dp, bottom = 122.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
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
                onNavigateAbout = {
                    showSettingsSheet = false
                    navController.navigate(com.kaizen.khushu.ui.navigation.SETTINGS_ABOUT_ROUTE)
                },
                onDismiss = { showSettingsSheet = false }
            )
        }

        if (showDeveloperWelcome) {
            DeveloperWelcomeDialog(
                onContinue = { settingsViewModel.setDeveloperWelcomeDismissed(true) },
                onOpenSupport = {
                    settingsViewModel.setDeveloperWelcomeDismissed(true)
                    navController.navigate(SETTINGS_ABOUT_ROUTE)
                }
            )
        }
    }
}

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private const val M3Duration = 500

// --- LATERAL TAB ANIMATIONS ---
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = 150,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        )
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        )
    ) + scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )

// --- VERTICAL SUB-SCREEN ANIMATIONS ---
private fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = 100,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        )
    ) + scaleIn(
        initialScale = 0.85f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        )
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenPopEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = 100,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        )
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.subScreenPopExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        )
    ) + scaleOut(
        targetScale = 0.85f,
        animationSpec = tween(M3Duration, easing = EmphasizedEasing)
    )
