package com.example.igccxi.screens.scoring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.domain.model.BallType
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.screens.scoring.components.BattersScoreCard
import com.example.igccxi.screens.scoring.components.BowlerDialog
import com.example.igccxi.screens.scoring.components.BowlerScoreCard
import com.example.igccxi.screens.scoring.components.EditOversDialog
import com.example.igccxi.screens.scoring.components.ExtrasDialog
import com.example.igccxi.screens.scoring.components.RecentDeliveriesSection
import com.example.igccxi.screens.scoring.components.ScoreBoardCard
import com.example.igccxi.screens.scoring.components.ScoringKeypad
import com.example.igccxi.screens.scoring.components.SecondInningsDialog
import com.example.igccxi.screens.scoring.components.WicketDialog
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketSuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoringScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScorecard: (String) -> Unit,
    viewModel: LiveScoringViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showEndInningsConfirmDialog by remember { mutableStateOf(false) }
    var showEditOversDialog by remember { mutableStateOf(false) }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match = uiState.match

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (match != null) "${match.teamA} vs ${match.teamB}" else "Live Scoring",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (match.isScorable) CricketSuccessGreen else Color.Gray,
                                    shape = CircleShape,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (match.isScorable) "LIVE SCORING" else match.status.name.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (match.isScorable) CricketSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (match != null) {
                        IconButton(onClick = { viewModel.swapStrike() }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Strike",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF Scorecard")
                        }
                        IconButton(onClick = { onNavigateToScorecard(match.id) }) {
                            Icon(Icons.Default.Assessment, contentDescription = "Full Scorecard")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            val innings = match.currentInnings
            val striker = innings.getStriker()
            val nonStriker = innings.getNonStriker()
            val bowler = innings.getCurrentBowler()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Free Hit Alert Banner (MCC Law: Free Hit follows No-Ball)
                val isFreeHit = match.isScorable && innings.recentBalls.lastOrNull()?.type == BallType.NO_BALL
                if (isFreeHit) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎯 FREE HIT NEXT BALL! (Cannot be out Bowled/Caught/LBW)",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 1. Stadium Score Card
                ScoreBoardCard(
                    match = match,
                    innings = innings,
                    isScorer = uiState.isScorer && match.isScorable,
                    onEditOversClick = { showEditOversDialog = true }
                )

                // 2. Active Batters Section
                BattersScoreCard(
                    striker = striker,
                    nonStriker = nonStriker,
                    onSwapStrike = { viewModel.swapStrike() }
                )

                // 3. Current Bowler Section
                BowlerScoreCard(
                    bowler = bowler,
                    onChangeBowlerClick = { viewModel.showBowlerDialog() }
                )

                // 4. Over Deliveries Ticker
                RecentDeliveriesSection(recentBalls = innings.recentBalls)

                // 5. Scorer Keypad or Transition States
                if (match.isScorable) {
                    ScoringKeypad(
                        canUndo = uiState.canUndo,
                        onDotBall = { viewModel.recordRuns(0) },
                        onRuns = { runs -> viewModel.recordRuns(runs) },
                        onWide = { viewModel.recordWide(1) },
                        onNoBall = { viewModel.recordNoBall(0) },
                        onExtrasClick = { viewModel.showExtrasDialog() },
                        onWicketClick = { viewModel.showWicketDialog() },
                        onUndoClick = { viewModel.undoLastBall() },
                        onEndInningsClick = { showEndInningsConfirmDialog = true },
                        inningsNumber = match.currentInningsNumber
                    )
                } else if (match.status == MatchStatus.INNINGS_BREAK) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "1st Innings Finished",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target: ${match.target ?: (innings.totalRuns + 1)} runs",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CricketGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.endInnings() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Start 2nd Innings Chasing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                } else if (match.status == MatchStatus.COMPLETED) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = CricketGreenPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Match Completed",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.result,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = CricketGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Button(
                                    onClick = { onNavigateToScorecard(match.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Scorecard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Dialogs ---
            if (showEndInningsConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showEndInningsConfirmDialog = false },
                    title = {
                        Text(
                            text = if (match.currentInningsNumber == 1) "End 1st Innings?" else "Finish Match?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            if (match.currentInningsNumber == 1)
                                "Are you sure you want to end the 1st innings? The target will be calculated automatically."
                            else
                                "Are you sure you want to finish and finalize this match scorecard?"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showEndInningsConfirmDialog = false
                                viewModel.endInnings()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndInningsConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (uiState.showWicketDialog) {
                WicketDialog(
                    existingBatterNames = innings.batters.map { it.name }.filter { it.isNotBlank() },
                    strikerName = striker?.name ?: "Striker",
                    nonStrikerName = nonStriker?.name ?: "Non-Striker",
                    currentWickets = innings.wickets,
                    maxWickets = match.maxWickets,
                    onDismiss = { viewModel.dismissWicketDialog() },
                    onConfirm = { dismissal, newBatter, isStrikerOut ->
                        viewModel.recordWicket(dismissal, newBatter, isStrikerOut)
                    }
                )
            }

            if (uiState.showExtrasDialog) {
                ExtrasDialog(
                    onDismiss = { viewModel.dismissExtrasDialog() },
                    onBye = { runs -> viewModel.recordBye(runs) },
                    onLegBye = { runs -> viewModel.recordLegBye(runs) },
                    onWide = { extraRuns -> viewModel.recordWide(extraRuns) },
                    onNoBall = { runsOffBat -> viewModel.recordNoBall(runsOffBat) }
                )
            }

            if (uiState.showBowlerDialog) {
                BowlerDialog(
                    existingBowlers = innings.bowlers.map { it.name }.distinct(),
                    currentBowlerName = bowler?.name ?: "",
                    isOverComplete = uiState.isOverCompleteBowlerChange,
                    overNumber = uiState.completedOverNumber,
                    onDismiss = { viewModel.dismissBowlerDialog() },
                    onConfirm = { name -> viewModel.selectBowler(name) }
                )
            }

            if (uiState.showSecondInningsDialog) {
                SecondInningsDialog(
                    battingTeam = innings.bowlingTeam,
                    onDismiss = { viewModel.dismissSecondInningsDialog() },
                    onConfirm = { strikerName, nonStrikerName, bowlerName ->
                        viewModel.startSecondInnings(strikerName, nonStrikerName, bowlerName)
                    }
                )
            }

            if (showEditOversDialog) {
                EditOversDialog(
                    currentOvers = match.totalOvers,
                    onDismiss = { showEditOversDialog = false },
                    onConfirm = { newOvers ->
                        showEditOversDialog = false
                        viewModel.updateMatchOvers(newOvers)
                    }
                )
            }
        }
    }
}
