package com.example.igccxi.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.igccxi.screens.create.CreateMatchScreen
import com.example.igccxi.screens.details.MatchDetailsScreen
import com.example.igccxi.screens.history.MatchHistoryScreen
import com.example.igccxi.screens.home.HomeScreen
import com.example.igccxi.screens.rankings.RankingsScreen
import com.example.igccxi.screens.scoring.LiveScoringScreen

private data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "Scorecards",
        route = Screen.Scorecards.route,
        selectedIcon = Icons.Filled.SportsCricket,
        unselectedIcon = Icons.Outlined.SportsCricket
    ),
    BottomNavItem(
        title = "Rankings",
        route = Screen.Rankings.route,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    )
)

/**
 * Main application navigation graph defining all screen destinations, route transitions, and arguments.
 * Hosts the bottom navigation bar for the 3 top-level destinations (Home, Scorecards, Rankings).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val topLevelRoutes = setOf(
        Screen.Home.route,
        Screen.Scorecards.route,
        Screen.Rankings.route
    )
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
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
                    onNavigateToScorecards = {
                        navController.navigate(Screen.Scorecards.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRankings = {
                        navController.navigate(Screen.Rankings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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

            composable(Screen.Scorecards.route) {
                MatchHistoryScreen(
                    canNavigateBack = false,
                    onNavigateToDetails = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    },
                    onNavigateToScoring = { matchId ->
                        navController.navigate(Screen.LiveScoring.createRoute(matchId))
                    }
                )
            }

            composable(Screen.Rankings.route) {
                RankingsScreen(
                    canNavigateBack = false
                )
            }
        }
    }
}
