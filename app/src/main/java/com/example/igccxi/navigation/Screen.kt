package com.example.igccxi.navigation

/**
 * Defines all navigation destinations and parameterized routes for the application.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateMatch : Screen("create_match")
    data object LiveScoring : Screen("live_scoring/{matchId}") {
        const val MATCH_ID_ARG = "matchId"
        fun createRoute(matchId: String) = "live_scoring/$matchId"
    }
    data object MatchDetails : Screen("match_details/{matchId}") {
        const val MATCH_ID_ARG = "matchId"
        fun createRoute(matchId: String) = "match_details/$matchId"
    }
    data object MatchHistory : Screen("match_history")
    data object Rankings : Screen("rankings")
}
