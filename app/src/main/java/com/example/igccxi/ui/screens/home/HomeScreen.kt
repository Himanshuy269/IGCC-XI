package com.example.igccxi.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.data.model.Match
import com.example.igccxi.ui.components.ScoreCardSummary

enum class HomeFilter {
    ALL, LIVE, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateMatch: () -> Unit,
    onNavigateToScoring: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = com.example.igccxi.R.drawable.app_logo),
                            contentDescription = "IGCC XI Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(width = 44.dp, height = 33.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IGCC XI Cricket",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cloud Synced",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = onNavigateToHistory,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calendar",
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
                text = { Text("Create Match", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Featured Live Match OR Hero Welcome Banner
                val featuredLiveMatch = uiState.liveMatches.firstOrNull()
                if (featuredLiveMatch != null) {
                    item {
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
                    item {
                        CleanHeroBanner(
                            totalMatchesCount = allMatches.size,
                            completedCount = uiState.recentMatches.size,
                            onCreateMatchClick = onNavigateToCreateMatch,
                            onNavigateToHistory = onNavigateToHistory
                        )
                    }
                }

                // 2. "How to Use" Quick Guide (Clean 3-step walkthrough so anyone understands)
                if (allMatches.isEmpty()) {
                    item {
                        HowItWorksCard(onCreateMatchClick = onNavigateToCreateMatch)
                    }
                }

                // 3. Filter Navigation Tabs
                if (allMatches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == HomeFilter.ALL,
                                onClick = { selectedFilter = HomeFilter.ALL },
                                label = { Text("All Matches (${allMatches.size})", fontWeight = FontWeight.SemiBold) }
                            )

                            FilterChip(
                                selected = selectedFilter == HomeFilter.LIVE,
                                onClick = { selectedFilter = HomeFilter.LIVE },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (uiState.liveMatches.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFD32F2F),
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text("Live (${uiState.liveMatches.size})", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            )

                            FilterChip(
                                selected = selectedFilter == HomeFilter.COMPLETED,
                                onClick = { selectedFilter = HomeFilter.COMPLETED },
                                label = { Text("Finished (${uiState.recentMatches.size})", fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }

                // 4. Matches List or Empty State
                if (displayedMatches.isEmpty()) {
                    item {
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

                item { Spacer(modifier = Modifier.height(72.dp)) } // padding for Floating Action Button
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Featured Live Match Card (Shown when a live match is in progress)
// ---------------------------------------------------------------------------
@Composable
private fun FeaturedLiveCard(
    match: Match,
    isScorer: Boolean,
    onOpenMatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenMatch),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = com.example.igccxi.R.drawable.card_hero_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // High-intensity photo scrim overlay (lightened to preserve image brightness & vibrance)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x20000000), // ~12% subtle top shadow to keep sky & sunbeams vivid
                                Color(0x45000000), // ~27% mid shadow
                                Color(0x75000000)  // ~45% bottom shadow for button contrast
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MATCH NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${match.totalOvers} Overs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Teams and Scores Display
                val inn = match.currentInnings
                Text(
                    text = "${match.teamA} vs ${match.teamB}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${inn.totalRuns}/${inn.wickets}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${inn.oversFormatted} ov)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Button(
                        onClick = onOpenMatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F5132)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isScorer) Icons.Default.Edit else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScorer) "Continue Scoring" else "Watch Live",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Clean Hero Banner (Shown when no active match or as home header)
// ---------------------------------------------------------------------------
@Composable
private fun CleanHeroBanner(
    totalMatchesCount: Int,
    completedCount: Int,
    onCreateMatchClick: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = com.example.igccxi.R.drawable.card_hero_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // High-intensity photo scrim overlay (lightened to preserve image brightness & vibrance)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x20000000), // ~12% subtle top shadow to keep sky & sunbeams vivid
                                Color(0x45000000), // ~27% mid shadow
                                Color(0x75000000)  // ~45% bottom shadow for button contrast
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "CRICKET LIVE SCORER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fast, Simple & Cloud-Saved",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Image(
                        painter = painterResource(id = com.example.igccxi.R.drawable.app_logo),
                        contentDescription = "IGCC XI Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(width = 64.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create a match, score ball-by-ball, and download official PDF scorecards anytime.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Match Counter Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$totalMatchesCount Recorded Matches",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onNavigateToHistory,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onCreateMatchClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0F5132)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// "How It Works" 3-Step Card (Makes the app 100% understandable for anyone)
// ---------------------------------------------------------------------------
@Composable
private fun HowItWorksCard(onCreateMatchClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "HOW IT WORKS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Score cricket in 3 simple steps:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1
            StepItem(
                number = "1",
                title = "Create a Match",
                description = "Enter Team A & Team B names, total overs, and select who bats first."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 2
            StepItem(
                number = "2",
                title = "Live Ball-by-Ball Scoring",
                description = "Tap runs (0–6), boundaries, extras (Wd/Nb), and wickets. Strike rotates automatically."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 3
            StepItem(
                number = "3",
                title = "Save, Sync & PDF Export",
                description = "Matches are retained in local storage & cloud. Download or share official PDF scorecards anytime."
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreateMatchClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Your First Match", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Refined Empty State Prompt
// ---------------------------------------------------------------------------
@Composable
private fun EmptyStatePrompt(
    selectedFilter: HomeFilter,
    onCreateMatchClick: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SportsCricket,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (selectedFilter) {
                    HomeFilter.LIVE -> "No Live Matches Right Now"
                    HomeFilter.COMPLETED -> "No Completed Matches Yet"
                    HomeFilter.ALL -> "No Matches Recorded"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (selectedFilter) {
                    HomeFilter.LIVE -> "Create a new match to start ball-by-ball scoring."
                    HomeFilter.COMPLETED -> "Finished matches will appear here with full scorecards."
                    HomeFilter.ALL -> "Start a new match or browse past scorecards from the calendar archive."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToHistory,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calendar Archive", fontSize = 12.sp)
                }

                Button(
                    onClick = onCreateMatchClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
