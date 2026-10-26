package com.example.igccxi.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.igccxi.screens.create.CreateMatchScreen
import com.example.igccxi.screens.details.MatchDetailsScreen
import com.example.igccxi.screens.history.MatchHistoryScreen
import com.example.igccxi.screens.home.HomeScreen
import com.example.igccxi.screens.rankings.RankingsScreen
import com.example.igccxi.screens.scoring.LiveScoringScreen

/**
 * Main application navigation graph defining all screen destinations, route transitions, and arguments.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(280)
            ) + fadeIn(animationSpec = tween(280))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(240)
            ) + fadeOut(animationSpec = tween(240))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(280)
            ) + fadeIn(animationSpec = tween(280))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(240)
            ) + fadeOut(animationSpec = tween(240))
        }
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
                },
                onNavigateToRankings = {
                    navController.navigate(Screen.Rankings.route)
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
            arguments = listOf(navArgument(Screen.LiveScoring.MATCH_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString(Screen.LiveScoring.MATCH_ID_ARG) ?: ""
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
            arguments = listOf(navArgument(Screen.MatchDetails.MATCH_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString(Screen.MatchDetails.MATCH_ID_ARG) ?: ""
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

        composable(Screen.Rankings.route) {
            RankingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
