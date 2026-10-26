package com.example.igccxi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketLiveRed
import com.example.igccxi.ui.theme.CricketSuccessGreen

@Composable
fun ScoreCardSummary(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLive = match.status == MatchStatus.INNINGS_1 || match.status == MatchStatus.INNINGS_2

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isLive) Brush.horizontalGradient(
                            listOf(Color(0xFF0F3B20), Color(0xFF195E34))
                        ) else Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = match.status, isLive = isLive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${match.totalOvers} Ov • ${match.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud Saved",
                        tint = if (isLive) Color.White.copy(alpha = 0.8f) else CricketSuccessGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Cloud",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) Color.White.copy(alpha = 0.8f) else CricketSuccessGreen
                    )
                }
            }

            // Teams and Scores Body
            Column(modifier = Modifier.padding(16.dp)) {
                val inn1 = match.innings1
                val inn2 = match.innings2

                // Team 1 Row
                val team1Name = if (inn1.battingTeam.trim().length > 1) inn1.battingTeam.trim() else match.teamA
                TeamScoreRow(
                    teamName = team1Name,
                    scoreText = if (inn1.totalLegalBalls > 0 || inn1.isCompleted) {
                        "${inn1.totalRuns}/${inn1.wickets}"
                    } else "Yet to bat",
                    oversText = if (inn1.totalLegalBalls > 0 || inn1.isCompleted) {
                        "(${inn1.oversFormatted} ov)"
                    } else "",
                    isBattingNow = match.currentInningsNumber == 1 && isLive
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Team 2 Row
                val team2Name = if (inn1.bowlingTeam.trim().length > 1) inn1.bowlingTeam.trim() else match.teamB
                TeamScoreRow(
                    teamName = team2Name,
                    scoreText = if (inn2 != null && (inn2.totalLegalBalls > 0 || inn2.isCompleted)) {
                        "${inn2.totalRuns}/${inn2.wickets}"
                    } else "Yet to bat",
                    oversText = if (inn2 != null && (inn2.totalLegalBalls > 0 || inn2.isCompleted)) {
                        "(${inn2.oversFormatted} ov)"
                    } else "",
                    isBattingNow = match.currentInningsNumber == 2 && isLive
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Status Line & Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val summaryText = when (match.status) {
                        MatchStatus.COMPLETED -> match.result.ifEmpty { "Match Finished" }
                        MatchStatus.INNINGS_BREAK -> "Innings Break • Target: ${match.target}"
                        MatchStatus.INNINGS_2 -> {
                            val needed = (match.target ?: 0) - (inn2?.totalRuns ?: 0)
                            val ballsLeft = (match.totalOvers * 6) - (inn2?.totalLegalBalls ?: 0)
                            if (needed > 0) "Needs $needed runs in $ballsLeft balls" else "Target achieved"
                        }
                        MatchStatus.INNINGS_1 -> "CRR: ${inn1.currentRunRateFormatted}"
                        MatchStatus.NOT_STARTED -> "Ready to start"
                    }

                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (match.status) {
                            MatchStatus.COMPLETED -> CricketGold
                            MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> CricketSuccessGreen
                            else -> MaterialTheme.colorScheme.primary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // PDF Quick Action
                        IconButton(
                            onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onClick)
                        ) {
                            Text(
                                text = if (isLive) "Score" else "Scorecard",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamScoreRow(
    teamName: String,
    scoreText: String,
    oversText: String,
    isBattingNow: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (isBattingNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = teamName.take(3).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBattingNow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isBattingNow) FontWeight.Black else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isBattingNow) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "BATTING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = scoreText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isBattingNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (oversText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = oversText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: MatchStatus, isLive: Boolean = false) {
    val (label, bg, fg) = when (status) {
        MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> Triple("LIVE", CricketLiveRed, Color.White)
        MatchStatus.INNINGS_BREAK -> Triple("BREAK", Color(0xFFFEF3C7), Color(0xFFB45309))
        MatchStatus.COMPLETED -> Triple("FINISHED", Color(0xFFE2E8F0), Color(0xFF334155))
        MatchStatus.NOT_STARTED -> Triple("READY", Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            if (isLive) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(5.dp)
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = fg,
                letterSpacing = 0.5.sp
            )
        }
    }
}
