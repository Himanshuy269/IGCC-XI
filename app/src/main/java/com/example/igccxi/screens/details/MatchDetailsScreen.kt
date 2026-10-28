package com.example.igccxi.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.ui.components.BallByBallTicker
import com.example.igccxi.ui.components.BattingTable
import com.example.igccxi.ui.components.BowlingTable
import com.example.igccxi.ui.components.StatusBadge
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScoring: (String) -> Unit,
    viewModel: MatchDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match = uiState.match
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = match?.let { "${it.teamA} vs ${it.teamB}" } ?: "Match Scorecard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (match != null) {
                        IconButton(onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (match != null) {
                ExtendedFloatingActionButton(
                    onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                    text = { Text("Export PDF", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading || match == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val scrollState = rememberScrollState()
            val isScorer = match.scorerId.isNotEmpty() && match.scorerId == uiState.currentUserId
            val hasSecondInnings = match.innings2 != null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Match Header Card (Stadium styling)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF072718), Color(0xFF0F5132), Color(0xFF14532D))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = match.status, isLive = match.isScorable)
                                val sideInfo = if (match.playersPerTeam != 11) " • ${match.playersPerTeam}s" else ""
                                Text(
                                    text = "${match.totalOvers} Overs$sideInfo • ${match.date}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Innings 1 Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val inn1Team = if (match.innings1.battingTeam.trim().length > 1) {
                                    match.innings1.battingTeam.trim()
                                } else match.teamA
                                Text(
                                    text = inn1Team,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${match.innings1.totalRuns}/${match.innings1.wickets} (${match.innings1.oversFormatted} ov)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            // Innings 2 Summary
                            if (match.innings2 != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val inn2Team = if (match.innings2.battingTeam.trim().length > 1) {
                                        match.innings2.battingTeam.trim()
                                    } else match.teamB
                                    Text(
                                        text = inn2Team,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${match.innings2.totalRuns}/${match.innings2.wickets} (${match.innings2.oversFormatted} ov)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Result / Status banner
                            val summaryText = when (match.status) {
                                MatchStatus.COMPLETED -> match.result.ifEmpty { "Match Completed" }
                                MatchStatus.INNINGS_BREAK -> "Innings Break • Target: ${match.target}"
                                MatchStatus.INNINGS_2 -> {
                                    val needed = (match.target ?: 0) - (match.innings2?.totalRuns ?: 0)
                                    val ballsLeft = (match.totalOvers * 6) - (match.innings2?.totalLegalBalls ?: 0)
                                    if (needed > 0) "Needs $needed runs in $ballsLeft balls" else "Target achieved"
                                }
                                MatchStatus.INNINGS_1 -> "1st Innings in progress • CRR: ${match.innings1.currentRunRateFormatted}"
                                MatchStatus.NOT_STARTED -> "Match ready to begin"
                            }

                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (match.status == MatchStatus.COMPLETED) CricketGold else Color.White
                            )

                            // Scorer quick resume button
                            if (isScorer && match.isScorable) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onNavigateToScoring(match.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = CricketGreenPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume Scoring", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Innings TabRow
                val tabs = if (hasSecondInnings) listOf("1st Innings", "2nd Innings") else listOf("1st Innings")
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }

                val currentInnings = if (selectedTabIndex == 1 && match.innings2 != null) {
                    match.innings2
                } else {
                    match.innings1
                }

                // Batting Table
                BattingTable(innings = currentInnings)

                // Bowling Table
                BowlingTable(innings = currentInnings)

                // Ball-by-ball ticker
                if (currentInnings.recentBalls.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "OVER BALL-BY-BALL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BallByBallTicker(recentBalls = currentInnings.recentBalls)
                    }
                }

                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
