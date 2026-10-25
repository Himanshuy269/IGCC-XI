package com.example.igccxi.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateMatch : Screen("create_match")
    object LiveScoring : Screen("live_scoring/{matchId}") {
        fun createRoute(matchId: String) = "live_scoring/$matchId"
    }
    object MatchDetails : Screen("match_details/{matchId}") {
        fun createRoute(matchId: String) = "match_details/$matchId"
    }
    object MatchHistory : Screen("match_history")
}
