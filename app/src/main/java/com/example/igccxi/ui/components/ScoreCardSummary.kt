package com.example.igccxi.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.model.MatchStatus
import com.example.igccxi.domain.PdfScorecardExporter

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
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isLive) Brush.horizontalGradient(
                            listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                        ) else Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = match.status, isLive = isLive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${match.totalOvers} Overs • ${match.date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud Saved",
                        tint = if (isLive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cloud",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Teams and Scores Body
            Column(modifier = Modifier.padding(16.dp)) {
                val inn1 = match.innings1
                val inn2 = match.innings2

                // Team 1 Row
                TeamScoreRow(
                    teamName = inn1.battingTeam.ifEmpty { match.teamA },
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
                TeamScoreRow(
                    teamName = inn1.bowlingTeam.ifEmpty { match.teamB },
                    scoreText = if (inn2 != null && (inn2.totalLegalBalls > 0 || inn2.isCompleted)) {
                        "${inn2.totalRuns}/${inn2.wickets}"
                    } else "Yet to bat",
                    oversText = if (inn2 != null && (inn2.totalLegalBalls > 0 || inn2.isCompleted)) {
                        "(${inn2.oversFormatted} ov)"
                    } else "",
                    isBattingNow = match.currentInningsNumber == 2 && isLive
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                        MatchStatus.NOT_STARTED -> "Upcoming match"
                    }

                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (match.status == MatchStatus.COMPLETED) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            Color(0xFF2E7D32)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // PDF Quick Action
                        IconButton(
                            onClick = { PdfScorecardExporter.generateAndSharePdf(context, match) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onClick)
                        ) {
                            Text(
                                text = if (isLive) "Score" else "Scorecard",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
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
            // Team Avatar Circle
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (isBattingNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = teamName.take(3).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBattingNow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isBattingNow) FontWeight.Bold else FontWeight.SemiBold
                    )
                    if (isBattingNow) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🏏",
                            fontSize = 12.sp
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
        MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> Triple("LIVE", Color.White, Color(0xFF1B5E20))
        MatchStatus.INNINGS_BREAK -> Triple("BREAK", Color(0xFFFFF8E1), Color(0xFFF57F17))
        MatchStatus.COMPLETED -> Triple("FINISHED", Color(0xFFEDE7F6), Color(0xFF512DA8))
        MatchStatus.NOT_STARTED -> Triple("UPCOMING", Color(0xFFECEFF1), Color(0xFF455A64))
    }

    SuggestionChip(
        onClick = {},
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLive) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(10.dp).padding(end = 2.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = fg
                )
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = bg),
        border = null,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(26.dp)
    )
}
