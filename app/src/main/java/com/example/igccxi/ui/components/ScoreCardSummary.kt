package com.example.igccxi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.model.MatchStatus

@Composable
fun ScoreCardSummary(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status and Date Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = match.status)

                Text(
                    text = "${match.date} • ${match.totalOvers} Overs",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Team Scores
            val inn1 = match.innings1
            val inn2 = match.innings2

            // Team 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inn1.battingTeam.ifEmpty { match.teamA },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (inn1.totalLegalBalls > 0 || inn1.isCompleted) {
                        "${inn1.totalRuns}/${inn1.wickets} (${inn1.oversFormatted} ov)"
                    } else "Yet to bat",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Team 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inn1.bowlingTeam.ifEmpty { match.teamB },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (inn2 != null && (inn2.totalLegalBalls > 0 || inn2.isCompleted)) {
                        "${inn2.totalRuns}/${inn2.wickets} (${inn2.oversFormatted} ov)"
                    } else "Yet to bat",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Result or Live summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val subtitle = when (match.status) {
                    MatchStatus.COMPLETED -> match.result.ifEmpty { "Match Finished" }
                    MatchStatus.INNINGS_BREAK -> "Innings Break • Target: ${match.target}"
                    MatchStatus.INNINGS_2 -> {
                        val needed = (match.target ?: 0) - (inn2?.totalRuns ?: 0)
                        val ballsRemaining = (match.totalOvers * 6) - (inn2?.totalLegalBalls ?: 0)
                        if (needed > 0) "Needs $needed runs in $ballsRemaining balls" else "Target achieved"
                    }
                    MatchStatus.INNINGS_1 -> "CRR: ${inn1.currentRunRateFormatted}"
                    MatchStatus.NOT_STARTED -> "Scheduled"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (match.status == MatchStatus.COMPLETED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Scorecard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View scorecard",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: MatchStatus) {
    val (label, bg, fg) = when (status) {
        MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> Triple("LIVE", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        MatchStatus.INNINGS_BREAK -> Triple("BREAK", Color(0xFFFFF8E1), Color(0xFFF57F17))
        MatchStatus.COMPLETED -> Triple("RESULT", Color(0xFFEDE7F6), Color(0xFF512DA8))
        MatchStatus.NOT_STARTED -> Triple("UPCOMING", Color(0xFFECEFF1), Color(0xFF455A64))
    }

    SuggestionChip(
        onClick = {},
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status == MatchStatus.INNINGS_1 || status == MatchStatus.INNINGS_2) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.padding(end = 4.dp).padding(top = 1.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = bg
        ),
        border = null,
        shape = RoundedCornerShape(8.dp)
    )
}
