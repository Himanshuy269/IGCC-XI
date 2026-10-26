package com.example.igccxi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.Innings

@Composable
fun BattingTable(
    innings: Innings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${innings.battingTeam} Batting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batter",
                    modifier = Modifier.weight(2.5f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "R",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "B",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "4s",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "6s",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SR",
                    modifier = Modifier.weight(1.0f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Batter Rows
            innings.batters.forEach { batter ->
                val isStriker = batter.id == innings.currentStrikerId
                val isNonStriker = batter.id == innings.currentNonStrikerId
                val statusText = when {
                    batter.isOut -> batter.dismissal.ifEmpty { "out" }
                    isStriker -> "batting *"
                    isNonStriker -> "not out"
                    else -> "yet to bat"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2.5f)) {
                        Text(
                            text = if (isStriker) "${batter.name} *" else batter.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isStriker || isNonStriker) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${batter.runs}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${batter.ballsFaced}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${batter.fours}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${batter.sixes}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = batter.strikeRateFormatted,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Extras & Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Extras",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${innings.extras.total} (w ${innings.extras.wides}, nb ${innings.extras.noBalls}, b ${innings.extras.byes}, lb ${innings.extras.legByes})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Score",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${innings.totalRuns}/${innings.wickets} (${innings.oversFormatted} ov)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
