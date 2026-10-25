package com.example.igccxi.ui.screens.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.data.model.BatterScore
import com.example.igccxi.data.model.BowlerScore
import com.example.igccxi.data.model.Innings
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.model.MatchStatus
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.ui.components.BallByBallTicker

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
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (match.isScorable) Color(0xFF2E7D32) else Color.Gray,
                                    shape = CircleShape,
                                    modifier = Modifier.size(7.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (match.isScorable) "LIVE SCORING" else match.status.name.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (match.isScorable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
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

                // 5. Scorer Keypad
                if (match.isScorable) {
                    ScoringKeypad(
                        canUndo = uiState.canUndo,
                        onDotBall = { viewModel.recordRuns(0) },
                        onRuns = { runs -> viewModel.recordRuns(runs) },
                        onWide = { viewModel.recordWide(1) },
                        onNoBall = { viewModel.recordNoBall(0) },
                        onWicketClick = { viewModel.showWicketDialog() },
                        onUndoClick = { viewModel.undoLastBall() },
                        onEndInningsClick = { showEndInningsConfirmDialog = true },
                        inningsNumber = match.currentInningsNumber
                    )
                } else if (match.status == MatchStatus.INNINGS_BREAK) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "1st Innings Completed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target: ${match.target ?: (innings.totalRuns + 1)} runs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.endInnings() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Start 2nd Innings Chasing", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (match.status == MatchStatus.COMPLETED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Match Completed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.result,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download PDF")
                                }
                                Button(
                                    onClick = { onNavigateToScorecard(match.id) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("View Scorecard")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                    onDismiss = { viewModel.dismissWicketDialog() },
                    onConfirm = { dismissal, newBatter ->
                        viewModel.recordWicket(dismissal, newBatter)
                    }
                )
            }

            if (uiState.showBowlerDialog) {
                BowlerDialog(
                    existingBowlers = innings.bowlers.map { it.name }.distinct(),
                    currentBowlerName = bowler?.name ?: "",
                    onDismiss = { viewModel.dismissBowlerDialog() },
                    onConfirm = { name -> viewModel.selectBowler(name) }
                )
            }

            if (uiState.showSecondInningsDialog) {
                SecondInningsDialog(
                    battingTeam = innings.bowlingTeam,
                    onDismiss = { viewModel.dismissSecondInningsDialog() },
                    onConfirm = { striker, nonStriker, bowlerName ->
                        viewModel.startSecondInnings(striker, nonStriker, bowlerName)
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

// ---------------------------------------------------------------------------
// 1. Stadium Score Board Card
// ---------------------------------------------------------------------------
@Composable
private fun ScoreBoardCard(
    match: Match,
    innings: Innings,
    isScorer: Boolean = false,
    onEditOversClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F5132), Color(0xFF198754), Color(0xFF1B4D3E))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                // Header Row: Batting team & innings tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsCricket,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = innings.battingTeam.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "${if (match.currentInningsNumber == 1) "1st" else "2nd"} Innings",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Score & Overs Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${innings.totalRuns}",
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " / ${innings.wickets}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (isScorer) Modifier.clickable { onEditOversClick() } else Modifier
                        ) {
                            Text(
                                text = "${innings.oversFormatted} / ${match.totalOvers} ov",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isScorer) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Match Overs",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CRR: ${innings.currentRunRateFormatted}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Chasing Target Banner in Innings 2
                if (match.currentInningsNumber == 2 && match.target != null) {
                    val needed = match.target - innings.totalRuns
                    val ballsLeft = (match.totalOvers * 6) - innings.totalLegalBalls
                    val rrr = if (ballsLeft > 0 && needed > 0) String.format(java.util.Locale.US, "%.2f", (needed.toDouble() * 6.0) / ballsLeft) else "0.0"

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (needed > 0) "Need $needed in $ballsLeft balls" else "Target Reached!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Target: ${match.target} • RRR: $rrr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Over Progress (6 Balls Indicator)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Over Progress (${innings.ballsBowledInOver}/6):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (i in 1..6) {
                            val isBowled = i <= innings.ballsBowledInOver
                            Surface(
                                shape = CircleShape,
                                color = if (isBowled) Color(0xFF81C784) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(10.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Active Batters Card
// ---------------------------------------------------------------------------
@Composable
private fun BattersScoreCard(
    striker: BatterScore?,
    nonStriker: BatterScore?,
    onSwapStrike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BATSMEN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                OutlinedButton(
                    onClick = onSwapStrike,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Swap Ends", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Table Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batter",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.8f)
                )
                Text(
                    text = "R",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "B",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "4s",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    text = "6s",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    text = "SR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Striker Row (Highlighted)
            BatterTableRow(
                name = striker?.name ?: "Striker",
                runs = striker?.runs ?: 0,
                balls = striker?.ballsFaced ?: 0,
                fours = striker?.fours ?: 0,
                sixes = striker?.sixes ?: 0,
                strikeRate = striker?.strikeRateFormatted ?: "0.0",
                isOnStrike = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Non-Striker Row
            BatterTableRow(
                name = nonStriker?.name ?: "Non-Striker",
                runs = nonStriker?.runs ?: 0,
                balls = nonStriker?.ballsFaced ?: 0,
                fours = nonStriker?.fours ?: 0,
                sixes = nonStriker?.sixes ?: 0,
                strikeRate = nonStriker?.strikeRateFormatted ?: "0.0",
                isOnStrike = false
            )
        }
    }
}

@Composable
private fun BatterTableRow(
    name: String,
    runs: Int,
    balls: Int,
    fours: Int,
    sixes: Int,
    strikeRate: String,
    isOnStrike: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isOnStrike) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOnStrike) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "On Strike",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = name + if (isOnStrike) " *" else "",
                    fontSize = 13.sp,
                    fontWeight = if (isOnStrike) FontWeight.Bold else FontWeight.Medium,
                    color = if (isOnStrike) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "$runs",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )
            Text(
                text = "$balls",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )
            Text(
                text = "$fours",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
            Text(
                text = "$sixes",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
            Text(
                text = strikeRate,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Current Bowler Card
// ---------------------------------------------------------------------------
@Composable
private fun BowlerScoreCard(
    bowler: BowlerScore?,
    onChangeBowlerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BOWLER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bowler?.name ?: "Current Bowler",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onChangeBowlerClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChangeCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bowler Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BowlerStatBadge(label = "OVERS", value = bowler?.oversFormatted ?: "0.0")
                BowlerStatBadge(label = "MAIDENS", value = "${bowler?.maidens ?: 0}")
                BowlerStatBadge(label = "RUNS", value = "${bowler?.runsConceded ?: 0}")
                BowlerStatBadge(label = "WICKETS", value = "${bowler?.wickets ?: 0}", isWicket = true)
                BowlerStatBadge(label = "ECON", value = bowler?.economyFormatted ?: "0.0")
            }
        }
    }
}

@Composable
private fun BowlerStatBadge(label: String, value: String, isWicket: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (isWicket && value != "0") Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// 4. Recent Deliveries Section
// ---------------------------------------------------------------------------
@Composable
private fun RecentDeliveriesSection(recentBalls: List<com.example.igccxi.data.model.BallEvent>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT DELIVERIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            if (recentBalls.isNotEmpty()) {
                Text(
                    text = "Last ${recentBalls.size.coerceAtMost(12)} balls",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        BallByBallTicker(recentBalls = recentBalls)
    }
}

// ---------------------------------------------------------------------------
// 5. Scoring Keypad Controls
// ---------------------------------------------------------------------------
@Composable
private fun ScoringKeypad(
    canUndo: Boolean,
    onDotBall: () -> Unit,
    onRuns: (Int) -> Unit,
    onWide: () -> Unit,
    onNoBall: () -> Unit,
    onWicketClick: () -> Unit,
    onUndoClick: () -> Unit,
    onEndInningsClick: () -> Unit,
    inningsNumber: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SCORING KEYPAD",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )

            // Row 1: Singles / Dots (0, 1, 2, 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoringButton(
                    label = "0",
                    subLabel = "Dot",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDotBall
                )

                ScoringButton(
                    label = "1",
                    subLabel = "Run",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onRuns(1) }
                )

                ScoringButton(
                    label = "2",
                    subLabel = "Runs",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onRuns(2) }
                )

                ScoringButton(
                    label = "3",
                    subLabel = "Runs",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onRuns(3) }
                )
            }

            // Row 2: Boundaries (4, 6) & Extras (Wd, Nb)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Four
                ScoringButton(
                    label = "4",
                    subLabel = "FOUR",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF1E88E5),
                    textColor = Color.White,
                    onClick = { onRuns(4) }
                )

                // Six
                ScoringButton(
                    label = "6",
                    subLabel = "SIX",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF6A1B9A),
                    textColor = Color.White,
                    onClick = { onRuns(6) }
                )

                // Wide
                ScoringButton(
                    label = "Wd",
                    subLabel = "+1 Wide",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFFFB300),
                    textColor = Color.Black,
                    onClick = onWide
                )

                // No Ball
                ScoringButton(
                    label = "Nb",
                    subLabel = "+1 NoBall",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFFF9800),
                    textColor = Color.Black,
                    onClick = onNoBall
                )
            }

            // Row 3: Wicket & Undo (High visibility actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWicketClick,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsCricket,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WICKET",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onUndoClick,
                    enabled = canUndo,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Undo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 4: End Innings / Match Completion Button
            OutlinedButton(
                onClick = onEndInningsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (inningsNumber == 1) "Declare / End 1st Innings" else "Finish Match",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ScoringButton(
    label: String,
    subLabel: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = subLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.85f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 6. Upgraded Wicket Dialog with Quick Dismissal Chips
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WicketDialog(
    onDismiss: () -> Unit,
    onConfirm: (dismissal: String, newBatterName: String) -> Unit
) {
    val commonDismissals = listOf("Bowled", "Caught", "LBW", "Run Out", "Stumped", "Hit Wicket")
    var selectedDismissal by remember { mutableStateOf("Caught") }
    var fielderBowlerDetail by remember { mutableStateOf("") }
    var newBatter by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Wicket", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Dismissal Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Quick Dismissal Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonDismissals.forEach { type ->
                        FilterChip(
                            selected = selectedDismissal == type,
                            onClick = { selectedDismissal = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFFD32F2F)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = fielderBowlerDetail,
                    onValueChange = { fielderBowlerDetail = it },
                    label = { Text("Details (e.g. c Fielder)") },
                    placeholder = { Text("Optional: fielder or details") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newBatter,
                    onValueChange = { newBatter = it },
                    label = { Text("Next Batter Name") },
                    placeholder = { Text("e.g. Next Batter") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullDismissal = if (fielderBowlerDetail.trim().isNotEmpty())
                        "${selectedDismissal.lowercase()} (${fielderBowlerDetail.trim()})"
                    else
                        selectedDismissal.lowercase()
                    onConfirm(fullDismissal, newBatter.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Confirm Wicket", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



// ---------------------------------------------------------------------------
// 7. Upgraded Bowler Dialog with Quick Existing Bowlers
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BowlerDialog(
    existingBowlers: List<String>,
    currentBowlerName: String,
    onDismiss: () -> Unit,
    onConfirm: (bowlerName: String) -> Unit
) {
    var bowlerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChangeCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Bowler", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (existingBowlers.isNotEmpty()) {
                    Text(
                        text = "Choose previous bowler:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingBowlers.forEach { name ->
                            FilterChip(
                                selected = bowlerName == name,
                                onClick = { bowlerName = name },
                                label = { Text(name, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = bowlerName,
                    onValueChange = { bowlerName = it },
                    label = { Text("Or Type Bowler Name") },
                    placeholder = { Text("e.g. Bowler Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bowlerName.trim().isNotEmpty()) {
                        onConfirm(bowlerName.trim())
                    }
                },
                enabled = bowlerName.trim().isNotEmpty()
            ) {
                Text("Select Bowler", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



// ---------------------------------------------------------------------------
// 8. Second Innings Setup Dialog
// ---------------------------------------------------------------------------
@Composable
fun SecondInningsDialog(
    battingTeam: String,
    onDismiss: () -> Unit,
    onConfirm: (striker: String, nonStriker: String, bowler: String) -> Unit
) {
    var striker by remember { mutableStateOf("") }
    var nonStriker by remember { mutableStateOf("") }
    var bowler by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Start $battingTeam Innings", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter opening batters and opening bowler:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = striker,
                    onValueChange = { striker = it },
                    label = { Text("Opening Striker") },
                    placeholder = { Text("e.g. Batter 1") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nonStriker,
                    onValueChange = { nonStriker = it },
                    label = { Text("Opening Non-Striker") },
                    placeholder = { Text("e.g. Batter 2") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bowler,
                    onValueChange = { bowler = it },
                    label = { Text("Opening Bowler") },
                    placeholder = { Text("e.g. Bowler 1") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(striker, nonStriker, bowler) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start Chasing", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// 9. Edit Match Overs Dialog (Mid-Match Revision)
// ---------------------------------------------------------------------------
@Composable
fun EditOversDialog(
    currentOvers: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var oversText by remember { mutableStateOf(currentOvers.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Revise Match Overs", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Adjust the total overs for this match (e.g. rain reduction or revised match length):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = oversText,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                        oversText = filtered
                        error = null
                    },
                    label = { Text("Total Match Overs") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 15, 20, 50).forEach { ov ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { oversText = ov.toString() }
                        ) {
                            Text(
                                text = "$ov ov",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = oversText.toIntOrNull()
                    if (parsed == null || parsed < 1 || parsed > 100) {
                        error = "Please enter valid overs between 1 and 100"
                    } else {
                        onConfirm(parsed)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Update Overs", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


