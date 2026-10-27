package com.example.igccxi.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.R
import com.example.igccxi.screens.home.components.ActiveUsersCornerBadge
import com.example.igccxi.screens.home.components.ActiveUsersInfoDialog
import com.example.igccxi.screens.home.components.CleanHeroBanner
import com.example.igccxi.screens.home.components.ClubOverviewStatsCard
import com.example.igccxi.screens.home.components.CricketRulesFeaturesCard
import com.example.igccxi.screens.home.components.FeaturedLiveCard
import com.example.igccxi.screens.home.components.HowItWorksCard
import com.example.igccxi.screens.home.components.LatestMatchResultCard
import com.example.igccxi.screens.updater.AppUpdateDialog
import com.example.igccxi.screens.updater.AppUpdateViewModel
import com.example.igccxi.screens.updater.InstallPermissionDialog
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketSuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateMatch: () -> Unit,
    onNavigateToScoring: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToScorecards: () -> Unit,
    onNavigateToRankings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    updateViewModel: AppUpdateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by updateViewModel.uiState.collectAsState()
    var showActiveUsersDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "IGCC XI Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(width = 42.dp, height = 32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IGCC XI Cricket",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showActiveUsersDialog = true }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CricketSuccessGreen,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.activeUsersCount == 1) "1 Active Now" else "${uiState.activeUsersCount} Active Now",
                                    fontSize = 11.sp,
                                    color = CricketSuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• Cloud Sync",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (updateState.isUpdateAvailable && updateState.updateInfo != null) {
                                updateViewModel.openUpdateDialog()
                            } else {
                                updateViewModel.checkForUpdate(isManual = true)
                            }
                        },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (updateState.isUpdateAvailable) {
                                    Badge(containerColor = CricketGold) {
                                        Text("!", color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (updateState.isChecking) Icons.Default.Refresh else Icons.Default.SystemUpdate,
                                contentDescription = "Check for Updates",
                                tint = if (updateState.isUpdateAvailable) CricketGold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateMatch,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Match", fontWeight = FontWeight.ExtraBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(4.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val totalMatches = uiState.liveMatches.size + uiState.recentMatches.size
                val featuredLiveMatch = uiState.liveMatches.firstOrNull()
                val latestCompletedMatch = uiState.recentMatches.firstOrNull()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Update Notification Banner (if update is available and dialog was dismissed)
                    if (updateState.isUpdateAvailable && !updateState.showUpdateDialog && updateState.updateInfo != null) {
                        item(key = "update_available_banner") {
                            Surface(
                                onClick = { updateViewModel.openUpdateDialog() },
                                shape = RoundedCornerShape(14.dp),
                                color = CricketGold.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CricketGold.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = null,
                                            tint = CricketGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Update Available: v${updateState.updateInfo?.latestVersionName}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Tap to review release notes and install",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CricketGold,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = "Update",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Featured Live Match Spotlight (if any match is live)
                    if (featuredLiveMatch != null) {
                        item(key = "featured_live") {
                            FeaturedLiveCard(
                                match = featuredLiveMatch,
                                isScorer = featuredLiveMatch.scorerId == uiState.currentUserId,
                                onOpenMatch = {
                                    if (featuredLiveMatch.scorerId == uiState.currentUserId && featuredLiveMatch.isScorable) {
                                        onNavigateToScoring(featuredLiveMatch.id)
                                    } else {
                                        onNavigateToDetails(featuredLiveMatch.id)
                                    }
                                }
                            )
                        }
                    }

                    // 3. Clean Hero Banner
                    item(key = "hero_banner") {
                        CleanHeroBanner(
                            totalMatchesCount = totalMatches,
                            completedCount = uiState.recentMatches.size,
                            activeUsersCount = uiState.activeUsersCount,
                            onCreateMatchClick = onNavigateToCreateMatch,
                            onNavigateToHistory = onNavigateToScorecards,
                            onNavigateToRankings = onNavigateToRankings
                        )
                    }

                    // 4. Club Overview & Quick Metric Stats Card
                    item(key = "club_stats_overview") {
                        ClubOverviewStatsCard(
                            totalMatches = totalMatches,
                            liveMatches = uiState.liveMatches.size,
                            completedMatches = uiState.recentMatches.size,
                            activeUsers = uiState.activeUsersCount,
                            onNavigateToScorecards = onNavigateToScorecards,
                            onNavigateToRankings = onNavigateToRankings
                        )
                    }

                    // 5. Latest Concluded Match Result (if available)
                    if (latestCompletedMatch != null) {
                        item(key = "latest_result") {
                            LatestMatchResultCard(
                                match = latestCompletedMatch,
                                onViewScorecard = { onNavigateToDetails(latestCompletedMatch.id) },
                                onViewAllScorecards = onNavigateToScorecards
                            )
                        }
                    }

                    // 6. Beginner Guide (if 0 matches exist) OR Rules & Features Guide
                    if (totalMatches == 0) {
                        item(key = "how_it_works") {
                            HowItWorksCard(onCreateMatchClick = onNavigateToCreateMatch)
                        }
                    } else {
                        item(key = "rules_features") {
                            CricketRulesFeaturesCard()
                        }
                    }

                    // 7. Bottom Spacer to avoid FAB overlap
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }

            // Dedicated Corner Section for Number of Active Users (Bottom-Start corner)
            ActiveUsersCornerBadge(
                activeUsersCount = uiState.activeUsersCount,
                onClick = { showActiveUsersDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }
    }

    // --- Active Users Presence Info Dialog ---
    if (showActiveUsersDialog) {
        ActiveUsersInfoDialog(
            activeUsersCount = uiState.activeUsersCount,
            onDismiss = { showActiveUsersDialog = false }
        )
    }

    // --- In-App Auto-Updater Dialogs ---
    val availableUpdateInfo = updateState.updateInfo
    if (updateState.showUpdateDialog && availableUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = availableUpdateInfo,
            currentVersionName = updateState.currentVersionName,
            isMandatory = updateState.isMandatory,
            downloadState = updateState.downloadState,
            onStartDownload = { updateViewModel.startDownload() },
            onInstallApk = { apkFile -> updateViewModel.installDownloadedApk(apkFile) },
            onDismiss = { updateViewModel.dismissUpdateDialog() }
        )
    }

    if (updateState.showPermissionDialog) {
        InstallPermissionDialog(
            onOpenSettings = { updateViewModel.openInstallPermissionSettings() },
            onDismiss = { updateViewModel.dismissPermissionDialog() }
        )
    }

    if (updateState.feedbackMessage != null) {
        AlertDialog(
            onDismissRequest = { updateViewModel.dismissFeedback() },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "App Update Check",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = updateState.feedbackMessage ?: "",
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { updateViewModel.dismissFeedback() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
