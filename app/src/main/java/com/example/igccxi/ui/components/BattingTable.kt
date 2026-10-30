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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val displayTitle = if (innings.battingTeam.trim().length > 1) "${innings.battingTeam.trim()} Batting" else "Batting Scorecard"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${innings.totalRuns}/${innings.wickets} (${innings.oversFormatted} ov)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 7.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BATTER",
                    modifier = Modifier.weight(2.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "R",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "B",
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "4s",
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "6s",
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "SR",
                    modifier = Modifier.weight(1.0f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Batter Rows
            innings.batters.forEach { batter ->
                val isStriker = batter.id == innings.currentStrikerId
                val isNonStriker = batter.id == innings.currentNonStrikerId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2.6f)) {
                        Text(
                            text = if (isStriker) "${batter.name} *" else batter.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isStriker || isNonStriker) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Status / Dismissal line
                        if (batter.isOut) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier.padding(end = 5.dp)
                                ) {
                                    Text(
                                        text = "OUT",
                                        color = Color(0xFFDC2626),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = batter.dismissal.ifEmpty { "out" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (isStriker) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.padding(end = 5.dp)
                                ) {
                                    Text(
                                        text = "BATTING",
                                        color = Color(0xFF16A34A),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "not out *",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isNonStriker) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.padding(end = 5.dp)
                                ) {
                                    Text(
                                        text = "NOT OUT",
                                        color = Color(0xFF16A34A),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "yet to bat",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${batter.runs}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "${batter.ballsFaced}",
                        modifier = Modifier.weight(0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${batter.fours}",
                        modifier = Modifier.weight(0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${batter.sixes}",
                        modifier = Modifier.weight(0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = batter.strikeRateFormatted,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extras Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extras",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${innings.extras.total} (w ${innings.extras.wides}, nb ${innings.extras.noBalls}, b ${innings.extras.byes}, lb ${innings.extras.legByes})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total Score Row
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${innings.totalRuns}/${innings.wickets} in ${innings.oversFormatted} ov (CRR: ${innings.currentRunRateFormatted})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
