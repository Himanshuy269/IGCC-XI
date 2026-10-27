package com.example.igccxi.screens.scoring.components

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import com.example.igccxi.ui.theme.CricketGold
import java.util.Locale

@Composable
fun ScoreBoardCard(
    match: Match,
    innings: Innings,
    isScorer: Boolean = false,
    onEditOversClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        val displayBattingTeam = if (innings.battingTeam.trim().length > 1) {
                            innings.battingTeam.trim()
                        } else {
                            match.currentBattingTeam.let { if (it.trim().length > 1) it.trim() else match.teamA }
                        }
                        Text(
                            text = displayBattingTeam.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val isLoneBatsman = innings.currentNonStrikerId.isBlank() && !innings.isCompleted && innings.wickets == match.maxWickets - 1
                        if (isLoneBatsman) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CricketGold.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "⚡ Lone Batsman",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (match.playersPerTeam != 11) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "${match.playersPerTeam}-a-side",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "${if (match.currentInningsNumber == 1) "1st" else "2nd"} Innings",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Score & Overs Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${innings.totalRuns}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " / ${innings.wickets}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 5.dp)
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
                                fontWeight = FontWeight.Black,
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
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Chasing Target Banner in Innings 2
                if (match.currentInningsNumber == 2 && match.target != null) {
                    val needed = match.target - innings.totalRuns
                    val ballsLeft = (match.totalOvers * 6) - innings.totalLegalBalls
                    val rrr = if (ballsLeft > 0 && needed > 0) String.format(Locale.US, "%.2f", (needed.toDouble() * 6.0) / ballsLeft) else "0.0"

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.16f),
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
                                fontWeight = FontWeight.Bold,
                                color = CricketGold
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
                        text = "Balls in current over (${innings.ballsBowledInOver}/6):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (i in 1..6) {
                            val isBowled = i <= innings.ballsBowledInOver
                            Surface(
                                shape = CircleShape,
                                color = if (isBowled) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(10.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
