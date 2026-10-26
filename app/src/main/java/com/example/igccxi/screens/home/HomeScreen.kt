package com.example.igccxi.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.igccxi.screens.home.components.CleanHeroBanner
import com.example.igccxi.screens.home.components.EmptyStatePrompt
import com.example.igccxi.screens.home.components.FeaturedLiveCard
import com.example.igccxi.screens.home.components.HowItWorksCard
import com.example.igccxi.screens.updater.AppUpdateDialog
import com.example.igccxi.screens.updater.AppUpdateSection
import com.example.igccxi.screens.updater.AppUpdateViewModel
import com.example.igccxi.screens.updater.InstallPermissionDialog
import com.example.igccxi.ui.components.ScoreCardSummary
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketLiveRed
import com.example.igccxi.ui.theme.CricketSuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateMatch: () -> Unit,
    onNavigateToScoring: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRankings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    updateViewModel: AppUpdateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by updateViewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = CricketSuccessGreen,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cloud Sync Active",
                                    fontSize = 11.sp,
                                    color = CricketSuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { updateViewModel.checkForUpdate(isManual = true) },
                        modifier = Modifier
                            .padding(end = 4.dp)
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

                    Surface(
                        onClick = onNavigateToRankings,
                        shape = RoundedCornerShape(10.dp),
                        color = CricketGold.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CricketGold.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Rankings & Leaderboard",
                                tint = CricketGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rankings",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        onClick = onNavigateToHistory,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar Archive",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Archive",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val allMatches = (uiState.liveMatches + uiState.recentMatches).distinctBy { it.id }
            val displayedMatches = when (selectedFilter) {
                HomeFilter.ALL -> allMatches
                HomeFilter.LIVE -> uiState.liveMatches
                HomeFilter.COMPLETED -> uiState.recentMatches
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 0. Update Notification Banner (if update is available and dialog was dismissed)
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

                // 1. Featured Live Match OR Hero Welcome Banner
                val featuredLiveMatch = uiState.liveMatches.firstOrNull()
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
                } else {
                    item(key = "hero_banner") {
                        CleanHeroBanner(
                            totalMatchesCount = allMatches.size,
                            completedCount = uiState.recentMatches.size,
                            onCreateMatchClick = onNavigateToCreateMatch,
                            onNavigateToHistory = onNavigateToHistory,
                            onNavigateToRankings = onNavigateToRankings
                        )
                    }
                }

                // 2. Beginner Guide if no matches exist
                if (allMatches.isEmpty()) {
                    item(key = "how_it_works") {
                        HowItWorksCard(onCreateMatchClick = onNavigateToCreateMatch)
                    }
                }

                // 3. Filter Navigation Tabs
                if (allMatches.isNotEmpty()) {
                    item(key = "filter_tabs") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == HomeFilter.ALL,
                                onClick = { selectedFilter = HomeFilter.ALL },
                                label = { Text("All (${allMatches.size})", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            FilterChip(
                                selected = selectedFilter == HomeFilter.LIVE,
                                onClick = { selectedFilter = HomeFilter.LIVE },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (uiState.liveMatches.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = CricketLiveRed,
                                                modifier = Modifier.size(7.dp)
                                            ) {}
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text("Live (${uiState.liveMatches.size})", fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            FilterChip(
                                selected = selectedFilter == HomeFilter.COMPLETED,
                                onClick = { selectedFilter = HomeFilter.COMPLETED },
                                label = { Text("Finished (${uiState.recentMatches.size})", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // 4. Matches List or Empty State
                if (displayedMatches.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyStatePrompt(
                            selectedFilter = selectedFilter,
                            onCreateMatchClick = onNavigateToCreateMatch,
                            onNavigateToHistory = onNavigateToHistory
                        )
                    }
                } else {
                    items(displayedMatches, key = { it.id }) { match ->
                        ScoreCardSummary(
                            match = match,
                            onClick = {
                                if (match.scorerId == uiState.currentUserId && match.isScorable) {
                                    onNavigateToScoring(match.id)
                                } else {
                                    onNavigateToDetails(match.id)
                                }
                            }
                        )
                    }
                }

                // 5. Dedicated In-App Update Section
                item(key = "app_update_section") {
                    AppUpdateSection(
                        uiState = updateState,
                        onCheckForUpdates = { updateViewModel.checkForUpdate(isManual = true) },
                        onOpenUpdateDialog = { updateViewModel.openUpdateDialog() }
                    )
                }

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(72.dp)) }
            }
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
}
