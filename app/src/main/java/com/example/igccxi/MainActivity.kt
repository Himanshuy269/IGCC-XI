package com.example.igccxi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.igccxi.ui.navigation.Screen
import com.example.igccxi.ui.screens.create.CreateMatchScreen
import com.example.igccxi.ui.screens.details.MatchDetailsScreen
import com.example.igccxi.ui.screens.history.MatchHistoryScreen
import com.example.igccxi.ui.screens.home.HomeScreen
import com.example.igccxi.ui.screens.scoring.LiveScoringScreen
import com.example.igccxi.ui.theme.IGCCXITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IGCCXITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CricketAppNavigation()
                }
            }
        }
    }
}

@Composable
fun CricketAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateMatch = {
                    navController.navigate(Screen.CreateMatch.route)
                },
                onNavigateToScoring = { matchId ->
                    navController.navigate(Screen.LiveScoring.createRoute(matchId))
                },
                onNavigateToDetails = { matchId ->
                    navController.navigate(Screen.MatchDetails.createRoute(matchId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.MatchHistory.route)
                }
            )
        }

        composable(Screen.CreateMatch.route) {
            CreateMatchScreen(
                onNavigateBack = { navController.popBackStack() },
                onMatchCreated = { matchId ->
                    navController.navigate(Screen.LiveScoring.createRoute(matchId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.LiveScoring.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            LiveScoringScreen(
                matchId = matchId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScorecard = { id ->
                    navController.navigate(Screen.MatchDetails.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.MatchDetails.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            MatchDetailsScreen(
                matchId = matchId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScoring = { id ->
                    navController.navigate(Screen.LiveScoring.createRoute(id))
                }
            )
        }

        composable(Screen.MatchHistory.route) {
            MatchHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { matchId ->
                    navController.navigate(Screen.MatchDetails.createRoute(matchId))
                }
            )
        }
    }
}