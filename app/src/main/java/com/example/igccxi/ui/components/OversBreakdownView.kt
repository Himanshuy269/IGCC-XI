package com.example.igccxi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.BallEvent
import com.example.igccxi.domain.model.BallType
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.ui.theme.CricketFourBlue
import com.example.igccxi.ui.theme.CricketSuccessGreen
import com.example.igccxi.ui.theme.CricketWicketRed

data class OverSummary(
    val overNumber: Int,
    val bowlerName: String,
    val batterNames: List<String>,
    val totalRuns: Int,
    val balls: List<BallEvent>
)

/**
 * Groups raw ball deliveries of an innings into Over-by-Over breakdowns.
 * Each over has up to 6 legal balls (plus any wides and no-balls).
 */
fun groupBallsIntoOvers(innings: Innings): List<OverSummary> {
    if (innings.recentBalls.isEmpty()) return emptyList()

    val overs = mutableListOf<OverSummary>()
    var currentOverNumber = 1
    var legalCount = 0
    var currentBalls = mutableListOf<BallEvent>()

    for (ball in innings.recentBalls) {
        currentBalls.add(ball)
        if (ball.isActuallyLegalBall) {
            legalCount++
            if (legalCount == 6) {
                overs.add(buildOverSummary(currentOverNumber, currentBalls, innings))
                currentOverNumber++
                legalCount = 0
                currentBalls = mutableListOf()
            }
        }
    }

    // Capture in-progress final over if incomplete
    if (currentBalls.isNotEmpty()) {
        overs.add(buildOverSummary(currentOverNumber, currentBalls, innings))
    }

    return overs
}

private fun buildOverSummary(
    overNumber: Int,
    balls: List<BallEvent>,
    innings: Innings
): OverSummary {
    val totalRuns = balls.sumOf { it.runsOffBat + it.extraRuns }

    // Find bowler name from the balls
    val bowlerId = balls.firstOrNull { it.bowlerId.isNotBlank() }?.bowlerId ?: ""
    val bowlerName = innings.bowlers.find { it.id == bowlerId }?.name?.ifBlank { "Bowler" } ?: "Bowler"

    // Find distinct batters who faced balls in this over, in order of delivery
    val batterNames = balls.map { it.batterId }
        .filter { it.isNotBlank() }
        .distinct()
        .mapNotNull { bId -> innings.batters.find { it.id == bId }?.name?.ifBlank { null } }
        .ifEmpty { listOf("Batter") }

    return OverSummary(
        overNumber = overNumber,
        bowlerName = bowlerName,
        batterNames = batterNames,
        totalRuns = totalRuns,
        balls = balls
    )
}

@Composable
fun OversBreakdownView(
    innings: Innings,
    modifier: Modifier = Modifier
) {
    val overs = remember(innings.recentBalls) {
        groupBallsIntoOvers(innings)
    }

    if (overs.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsCricket,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Overs Recorded Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ball-by-ball deliveries will appear here as the innings progresses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Display latest over first (Ov 8, Ov 7, ... Ov 1) matching the reference scorecard
    val reversedOvers = remember(overs) { overs.reversed() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            reversedOvers.forEachIndexed { index, over ->
                OverRowItem(over = over)
                if (index < reversedOvers.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}

@Composable
fun OverRowItem(
    over: OverSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Over number and Total Runs
        Column(
            modifier = Modifier.width(66.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ov ${over.overNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            val runsLabel = if (over.totalRuns == 1) "1 Run" else "${over.totalRuns} Runs"
            Text(
                text = runsLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right Column: Bowler vs Batters title + Ball Badges Row
        Column(
            modifier = Modifier.weight(1f)
        ) {
            val batterText = over.batterNames.joinToString(" & ")
            val titleText = if (batterText.isNotBlank()) {
                "${over.bowlerName} to $batterText"
            } else {
                over.bowlerName
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally scrollable row of circular ball badges
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                over.balls.forEach { ball ->
                    BallDeliveryCircle(ball = ball)
                }
            }
        }
    }
}

/**
 * Individual ball delivery circle badge matching cricket scoring convention:
 * - Wicket: Red circle with "OUT"
 * - Six: Green circle with "6"
 * - Four: Blue circle with "4"
 * - Dots: White circle with "0"
 * - Runs: White circle with "1", "2", "3"
 * - Extras: Label below badge ("WD", "NB", "LB", "B")
 */
@Composable
fun BallDeliveryCircle(
    ball: BallEvent,
    modifier: Modifier = Modifier
) {
    val isWicket = ball.isWicket
    val runs = ball.runsOffBat
    val isSix = runs == 6
    val isFour = runs == 4

    val extraLabel: String? = when (ball.type) {
        BallType.WIDE -> "WD"
        BallType.NO_BALL -> "NB"
        BallType.BYE -> "B"
        BallType.LEG_BYE -> "LB"
        else -> null
    }

    val badgeBgColor: Color
    val badgeTextColor: Color
    val badgeBorderColor: Color
    val displayText: String

    when {
        isWicket -> {
            badgeBgColor = CricketWicketRed
            badgeTextColor = Color.White
            badgeBorderColor = Color.Transparent
            displayText = "OUT"
        }
        isSix -> {
            badgeBgColor = CricketSuccessGreen
            badgeTextColor = Color.White
            badgeBorderColor = Color.Transparent
            displayText = "6"
        }
        isFour -> {
            badgeBgColor = CricketFourBlue
            badgeTextColor = Color.White
            badgeBorderColor = Color.Transparent
            displayText = "4"
        }
        ball.type == BallType.WIDE -> {
            badgeBgColor = MaterialTheme.colorScheme.surface
            badgeTextColor = MaterialTheme.colorScheme.onSurface
            badgeBorderColor = MaterialTheme.colorScheme.outlineVariant
            // If wide was a dot (just 1 wide), display 0. If extra runs were taken off wide, show them.
            val wideRuns = if (ball.extraRuns > 1) "${ball.extraRuns - 1}" else "0"
            displayText = wideRuns
        }
        ball.type == BallType.NO_BALL -> {
            badgeBgColor = MaterialTheme.colorScheme.surface
            badgeTextColor = MaterialTheme.colorScheme.onSurface
            badgeBorderColor = MaterialTheme.colorScheme.outlineVariant
            displayText = if (runs > 0) "$runs" else "0"
        }
        ball.type == BallType.BYE || ball.type == BallType.LEG_BYE -> {
            badgeBgColor = MaterialTheme.colorScheme.surface
            badgeTextColor = MaterialTheme.colorScheme.onSurface
            badgeBorderColor = MaterialTheme.colorScheme.outlineVariant
            displayText = "${ball.extraRuns}"
        }
        else -> {
            badgeBgColor = MaterialTheme.colorScheme.surface
            badgeTextColor = MaterialTheme.colorScheme.onSurface
            badgeBorderColor = MaterialTheme.colorScheme.outlineVariant
            displayText = "$runs"
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(badgeBgColor, CircleShape)
                .border(
                    width = if (badgeBorderColor == Color.Transparent) 0.dp else 1.dp,
                    color = badgeBorderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                color = badgeTextColor,
                fontSize = if (displayText.length > 2) 10.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (extraLabel != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = extraLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(13.dp))
        }
    }
}
