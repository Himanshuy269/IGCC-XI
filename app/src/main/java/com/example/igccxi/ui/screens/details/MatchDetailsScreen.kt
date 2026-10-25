package com.example.igccxi.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.data.model.MatchStatus
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.ui.components.BallByBallTicker
import com.example.igccxi.ui.components.BattingTable
import com.example.igccxi.ui.components.BowlingTable
import com.example.igccxi.ui.components.StatusBadge

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
        topBar = {
            TopAppBar(
                title = { Text(text = match?.let { "${it.teamA} vs ${it.teamB}" } ?: "Scorecard", fontWeight = FontWeight.Bold) },
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
                }
            )
        },
        floatingActionButton = {
            if (match != null) {
                ExtendedFloatingActionButton(
                    onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                    text = { Text("Download PDF") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Match Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = match.status)
                            Text(
                                text = "${match.date} • ${match.totalOvers} Overs",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Innings 1 Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = match.innings1.battingTeam.ifEmpty { match.teamA },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${match.innings1.totalRuns}/${match.innings1.wickets} (${match.innings1.oversFormatted} ov)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Innings 2 Summary
                        if (match.innings2 != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = match.innings2.battingTeam.ifEmpty { match.teamB },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${match.innings2.totalRuns}/${match.innings2.wickets} (${match.innings2.oversFormatted} ov)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Result / Status text
                        val summaryText = when (match.status) {
                            MatchStatus.COMPLETED -> match.result
                            MatchStatus.INNINGS_BREAK -> "Innings Break • Target: ${match.target}"
                            MatchStatus.INNINGS_2 -> {
                                val needed = (match.target ?: 0) - (match.innings2?.totalRuns ?: 0)
                                val ballsLeft = (match.totalOvers * 6) - (match.innings2?.totalLegalBalls ?: 0)
                                if (needed > 0) "Needs $needed runs in $ballsLeft balls" else "Target achieved"
                            }
                            MatchStatus.INNINGS_1 -> "CRR: ${match.innings1.currentRunRateFormatted}"
                            MatchStatus.NOT_STARTED -> "Match scheduled"
                        }

                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // If user is scorer and match is active, offer "Score Match"
                        if (isScorer && match.isScorable) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigateToScoring(match.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume Scoring")
                            }
                        }
                    }
                }

                // Innings TabRow
                val tabs = if (hasSecondInnings) listOf("1st Innings", "2nd Innings") else listOf("1st Innings")
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) }
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
                    Text(
                        text = "Recent Balls",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    BallByBallTicker(recentBalls = currentInnings.recentBalls)
                }

                Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
            }
        }
    }
}
