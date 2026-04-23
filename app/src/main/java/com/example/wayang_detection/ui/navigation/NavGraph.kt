package com.example.wayang_detection.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.wayang_detection.ui.screens.*
import com.example.wayang_detection.ui.theme.BgPrimary
import com.example.wayang_detection.viewmodel.DetectionViewModel
import com.example.wayang_detection.viewmodel.SettingsViewModel

/**
 * Root navigation graph: Splash → Main (with bottom tabs).
 * Main contains all screens with conditional bottom bar visibility.
 */
@Composable
fun RootNavGraph() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    rootNavController.navigate("main") {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen()
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel()
    val detectionViewModel: DetectionViewModel = viewModel()

    // Collect settings state
    val devModeEnabled by settingsViewModel.devModeEnabled.collectAsState()
    val confidenceThreshold by settingsViewModel.confidenceThreshold.collectAsState()
    val inputResolution by settingsViewModel.inputResolution.collectAsState()

    // Collect detection state
    val detectionState by detectionViewModel.detectionState.collectAsState()
    val fps by detectionViewModel.fps.collectAsState()
    val inferenceTimeMs by detectionViewModel.inferenceTimeMs.collectAsState()
    val frameAspectRatio by detectionViewModel.frameAspectRatio.collectAsState()

    // Collect AI state
    val aiResponse by detectionViewModel.aiResponse.collectAsState()
    val aiLoading by detectionViewModel.aiLoading.collectAsState()
    val aiError by detectionViewModel.aiError.collectAsState()

    // Determine if bottom bar should be visible
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Encyclopedia.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        },
        containerColor = BgPrimary
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .background(BgPrimary)
                .padding(paddingValues),
            enterTransition = {
                fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(200))
            },
            popEnterTransition = {
                fadeIn(tween(250))
            },
            popExitTransition = {
                fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 }
            }
        ) {
            // ── Home ──
            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenCamera = {
                        navController.navigate(Screen.Detection.createRoute("live"))
                    },
                    onOpenGallery = {
                        navController.navigate(Screen.Detection.createRoute("gallery"))
                    },
                    devModeEnabled = devModeEnabled,
                    onDevModeToggle = { settingsViewModel.toggleDevMode() },
                    confidenceThreshold = confidenceThreshold,
                    onConfidenceChange = { settingsViewModel.setConfidenceThreshold(it) },
                    inputResolution = inputResolution,
                    onResolutionChange = { settingsViewModel.setInputResolution(it) }
                )
            }

            // ── Detection ──
            composable(
                route = Screen.Detection.route,
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "live"
                val liveResults by detectionViewModel.liveResults.collectAsState()

                // Sync confidence threshold from settings to detection engine
                detectionViewModel.confidenceThreshold = confidenceThreshold

                DetectionScreen(
                    mode = mode,
                    onBack = { navController.popBackStack() },
                    onNavigateToResult = {
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    detectionState = detectionState,
                    liveResults = liveResults,
                    onProcessFrame = { imageProxy -> detectionViewModel.processFrame(imageProxy) },
                    onDetectFromUri = { uri -> detectionViewModel.detectFromUri(uri) },
                    onCaptureResults = { detectionViewModel.captureCurrentResults() },
                    onResetDetection = { detectionViewModel.resetDetection() },
                    devModeEnabled = devModeEnabled,
                    fps = fps,
                    inferenceTimeMs = inferenceTimeMs,
                    inputResolution = inputResolution,
                    confidenceThreshold = confidenceThreshold,
                    frameAspectRatio = frameAspectRatio
                )
            }

            // ── Result ──
            composable(route = Screen.Result.route) {
                val capturedResults by detectionViewModel.capturedResults.collectAsState()
                val capturedImage by detectionViewModel.capturedImage.collectAsState()
                ResultScreen(
                    detectionResults = capturedResults,
                    capturedImage = capturedImage,
                    onBack = { navController.popBackStack() },
                    onViewInEncyclopedia = { id ->
                        navController.navigate(Screen.CharacterDetail.createRoute(id))
                    },
                    onAskAiElaborate = { characterId ->
                        detectionViewModel.askAiElaborate(characterId)
                    },
                    aiResponse = aiResponse,
                    aiLoading = aiLoading,
                    aiError = aiError,
                    onClearAiResponse = { detectionViewModel.clearAiResponse() }
                )
            }

            // ── Encyclopedia ──
            composable(Screen.Encyclopedia.route) {
                EncyclopediaScreen(
                    onCharacterClick = { characterId ->
                        navController.navigate(Screen.CharacterDetail.createRoute(characterId))
                    }
                )
            }

            // ── Character Detail ──
            composable(
                route = Screen.CharacterDetail.route,
                arguments = listOf(navArgument("characterId") { type = NavType.StringType })
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
                CharacterDetailScreen(
                    characterId = characterId,
                    onBack = { navController.popBackStack() },
                    onAskAi = { charId, question ->
                        detectionViewModel.askAiQuestion(charId, question)
                    },
                    aiResponse = aiResponse,
                    aiLoading = aiLoading,
                    aiError = aiError,
                    onClearAiResponse = { detectionViewModel.clearAiResponse() }
                )
            }
        }
    }
}
