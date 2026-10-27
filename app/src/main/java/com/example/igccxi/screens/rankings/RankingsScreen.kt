package com.example.igccxi.screens.rankings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.domain.model.PlayerBattingRanking
import com.example.igccxi.domain.model.PlayerBowlingRanking
import com.example.igccxi.ui.theme.CricketFourBlue
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketSixPurple
import com.example.igccxi.ui.theme.CricketWicketRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsScreen(
    canNavigateBack: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: RankingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leaderboard & Rankings",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${uiState.totalMatchesAnalyzed} Matches Analyzed • Unified Unique Players",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (canNavigateBack && onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Primary Tab Switcher: Batsmen vs Bowlers
                PrimaryTabRow(
                    selectedTabIndex = if (uiState.selectedTab == RankingTab.BATSMEN) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == RankingTab.BATSMEN,
                        onClick = { viewModel.onTabSelected(RankingTab.BATSMEN) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏏 Top Batsmen", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (uiState.allBatsmen.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${uiState.allBatsmen.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Tab(
                        selected = uiState.selectedTab == RankingTab.BOWLERS,
                        onClick = { viewModel.onTabSelected(RankingTab.BOWLERS) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚾ Top Bowlers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (uiState.allBowlers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${uiState.allBowlers.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2. Info Banner: Unique Player Deduplication Guarantee
                    item(key = "info_banner") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unique Player Rankings: Each player appears exactly once with aggregated stats across all match scorecards.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // 3. Search Bar
                    item(key = "search_field") {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search player by name...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. Sort Filter Chips Row
                    item(key = "sort_chips") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sort by:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (uiState.selectedTab == RankingTab.BATSMEN) {
                                FilterChip(
                                    selected = uiState.sortByBatsmen == BatsmenSort.RUNS,
                                    onClick = { viewModel.onBatsmenSortChanged(BatsmenSort.RUNS) },
                                    label = { Text("Most Runs", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.sortByBatsmen == BatsmenSort.STRIKE_RATE,
                                    onClick = { viewModel.onBatsmenSortChanged(BatsmenSort.STRIKE_RATE) },
                                    label = { Text("Strike Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.sortByBatsmen == BatsmenSort.SIXES,
                                    onClick = { viewModel.onBatsmenSortChanged(BatsmenSort.SIXES) },
                                    label = { Text("Most 6s", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            } else {
                                FilterChip(
                                    selected = uiState.sortByBowlers == BowlersSort.WICKETS,
                                    onClick = { viewModel.onBowlersSortChanged(BowlersSort.WICKETS) },
                                    label = { Text("Most Wickets", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.sortByBowlers == BowlersSort.ECONOMY,
                                    onClick = { viewModel.onBowlersSortChanged(BowlersSort.ECONOMY) },
                                    label = { Text("Best Economy", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.sortByBowlers == BowlersSort.MAIDENS,
                                    onClick = { viewModel.onBowlersSortChanged(BowlersSort.MAIDENS) },
                                    label = { Text("Maidens", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // 5. Leaderboard Content
                    if (uiState.selectedTab == RankingTab.BATSMEN) {
                        if (uiState.filteredBatsmen.isEmpty()) {
                            item(key = "empty_batsmen") {
                                EmptyRankingsPrompt(
                                    message = if (uiState.searchQuery.isNotEmpty()) "No batsmen found matching '${uiState.searchQuery}'" else "No batting scorecards recorded yet. Score matches to generate player rankings!"
                                )
                            }
                        } else {
                            // Top 3 Podium (Shown only when not searching)
                            if (uiState.searchQuery.isEmpty() && uiState.filteredBatsmen.size >= 3) {
                                item(key = "batsmen_podium") {
                                    BatsmenPodium(
                                        first = uiState.filteredBatsmen[0],
                                        second = uiState.filteredBatsmen[1],
                                        third = uiState.filteredBatsmen[2]
                                    )
                                }
                            }

                            // Full list of batsmen
                            items(uiState.filteredBatsmen, key = { "batter_${it.name}" }) { batter ->
                                BatsmanRankingCard(batter = batter)
                            }
                        }
                    } else {
                        if (uiState.filteredBowlers.isEmpty()) {
                            item(key = "empty_bowlers") {
                                EmptyRankingsPrompt(
                                    message = if (uiState.searchQuery.isNotEmpty()) "No bowlers found matching '${uiState.searchQuery}'" else "No bowling scorecards recorded yet. Score matches to generate bowler rankings!"
                                )
                            }
                        } else {
                            // Top 3 Podium (Shown only when not searching)
                            if (uiState.searchQuery.isEmpty() && uiState.filteredBowlers.size >= 3) {
                                item(key = "bowler_podium") {
                                    BowlerPodium(
                                        first = uiState.filteredBowlers[0],
                                        second = uiState.filteredBowlers[1],
                                        third = uiState.filteredBowlers[2]
                                    )
                                }
                            }

                            // Full list of bowlers
                            items(uiState.filteredBowlers, key = { "bowler_${it.name}" }) { bowler ->
                                BowlerRankingCard(bowler = bowler)
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * Podium card highlight for the top 3 batsmen.
 */
@Composable
fun BatsmenPodium(
    first: PlayerBattingRanking,
    second: PlayerBattingRanking,
    third: PlayerBattingRanking
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = CricketGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ORANGE CAP LEADERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = CricketGold,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "Top 3 Batsmen",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                PodiumPillar(
                    rank = 2,
                    playerName = second.name,
                    primaryStat = "${second.totalRuns} Runs",
                    secondaryStat = "SR ${second.strikeRateFormatted}",
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f)
                )

                // 1st Place (Gold Champion)
                PodiumPillar(
                    rank = 1,
                    playerName = first.name,
                    primaryStat = "${first.totalRuns} Runs",
                    secondaryStat = "HS ${first.highestScoreFormatted}",
                    color = CricketGold,
                    isChampion = true,
                    modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                PodiumPillar(
                    rank = 3,
                    playerName = third.name,
                    primaryStat = "${third.totalRuns} Runs",
                    secondaryStat = "SR ${third.strikeRateFormatted}",
                    color = Color(0xFFD97706).copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Podium card highlight for the top 3 bowlers.
 */
@Composable
fun BowlerPodium(
    first: PlayerBowlingRanking,
    second: PlayerBowlingRanking,
    third: PlayerBowlingRanking
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = CricketGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PURPLE CAP LEADERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = CricketGreenPrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "Top 3 Bowlers",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                PodiumPillar(
                    rank = 2,
                    playerName = second.name,
                    primaryStat = "${second.wickets} Wkts",
                    secondaryStat = "Econ ${second.economyFormatted}",
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f)
                )

                // 1st Place (Gold Champion)
                PodiumPillar(
                    rank = 1,
                    playerName = first.name,
                    primaryStat = "${first.wickets} Wkts",
                    secondaryStat = "Best ${first.bestBowlingFormatted}",
                    color = CricketGold,
                    isChampion = true,
                    modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                PodiumPillar(
                    rank = 3,
                    playerName = third.name,
                    primaryStat = "${third.wickets} Wkts",
                    secondaryStat = "Econ ${third.economyFormatted}",
                    color = Color(0xFFD97706).copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    rank: Int,
    playerName: String,
    primaryStat: String,
    secondaryStat: String,
    color: Color,
    isChampion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isChampion) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isChampion) 1.5.dp else 1.dp,
            color = if (isChampion) color else Color.Transparent
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(if (isChampion) 30.dp else 24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isChampion) 12.sp else 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = playerName,
                fontSize = if (isChampion) 13.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = primaryStat,
                fontSize = if (isChampion) 15.sp else 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isChampion) color else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = secondaryStat,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual Batsman ranking row card.
 */
@Composable
fun BatsmanRankingCard(batter: PlayerBattingRanking) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = when (batter.rank) {
                    1 -> CricketGold
                    2 -> Color(0xFF94A3B8)
                    3 -> Color(0xFFD97706)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${batter.rank}",
                        color = if (batter.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = batter.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${batter.inningsCount} Inns",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "HS: ${batter.highestScoreFormatted}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SR: ${batter.strikeRateFormatted}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "4s: ${batter.fours}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CricketFourBlue
                    )
                    Text(
                        text = "6s: ${batter.sixes}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CricketSixPurple
                    )
                    if (batter.fifties > 0 || batter.hundreds > 0) {
                        Text(
                            text = "50s: ${batter.fifties} | 100s: ${batter.hundreds}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGold
                        )
                    }
                }
            }

            // Runs Highlight
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${batter.totalRuns}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = CricketGreenPrimary
                )
                Text(
                    text = "${batter.ballsFaced} balls",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual Bowler ranking row card.
 */
@Composable
fun BowlerRankingCard(bowler: PlayerBowlingRanking) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = when (bowler.rank) {
                    1 -> CricketGold
                    2 -> Color(0xFF94A3B8)
                    3 -> Color(0xFFD97706)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${bowler.rank}",
                        color = if (bowler.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bowler.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${bowler.inningsCount} Inns",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${bowler.oversFormatted} ov",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Econ: ${bowler.economyFormatted}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Best: ${bowler.bestBowlingFormatted}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CricketGold
                    )
                    Text(
                        text = "Runs: ${bowler.runsConceded}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (bowler.maidens > 0) {
                        Text(
                            text = "Mdns: ${bowler.maidens}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketGreenPrimary
                        )
                    }
                }
            }

            // Wickets Highlight
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${bowler.wickets}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = CricketWicketRed
                )
                Text(
                    text = "Wickets",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyRankingsPrompt(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SportsCricket,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Player Rankings Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
