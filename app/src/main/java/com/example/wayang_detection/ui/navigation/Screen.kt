package com.example.wayang_detection.ui.navigation

/**
 * All screen routes in WayangVision app.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Detection : Screen("detection/{mode}") {
        fun createRoute(mode: String) = "detection/$mode"
    }
    data object Result : Screen("result/{characterId}/{confidence}") {
        fun createRoute(characterId: String, confidence: Float) =
            "result/$characterId/$confidence"
    }
    data object Encyclopedia : Screen("encyclopedia")
    data object CharacterDetail : Screen("character/{characterId}") {
        fun createRoute(characterId: String) = "character/$characterId"
    }
}
