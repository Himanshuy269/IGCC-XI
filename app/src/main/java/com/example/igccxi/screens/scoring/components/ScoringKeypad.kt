package com.example.igccxi.screens.scoring.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.ui.theme.CricketExtraAmber
import com.example.igccxi.ui.theme.CricketFourBlue
import com.example.igccxi.ui.theme.CricketSixPurple
import com.example.igccxi.ui.theme.CricketWicketRed

@Composable
fun ScoringKeypad(
    canUndo: Boolean,
    onDotBall: () -> Unit,
    onRuns: (Int) -> Unit,
    onWide: () -> Unit,
    onNoBall: () -> Unit,
    onExtrasClick: () -> Unit = {},
    onWicketClick: () -> Unit,
    onUndoClick: () -> Unit,
    onEndInningsClick: () -> Unit,
    inningsNumber: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCORING KEYPAD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Tap to record ball",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Row 1: Singles / Dots (0, 1, 2, 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoringButton(
                    label = "0",
                    subLabel = "DOT",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDotBall
                )

                ScoringButton(
                    label = "1",
                    subLabel = "RUN",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onRuns(1) }
                )

                ScoringButton(
                    label = "2",
                    subLabel = "RUNS",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onRuns(2) }
                )

                ScoringButton(
                    label = "3",
                    subLabel = "RUNS",
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
                ScoringButton(
                    label = "4",
                    subLabel = "FOUR",
                    modifier = Modifier.weight(1f),
                    backgroundColor = CricketFourBlue,
                    textColor = Color.White,
                    onClick = { onRuns(4) }
                )

                ScoringButton(
                    label = "6",
                    subLabel = "SIX",
                    modifier = Modifier.weight(1f),
                    backgroundColor = CricketSixPurple,
                    textColor = Color.White,
                    onClick = { onRuns(6) }
                )

                ScoringButton(
                    label = "Wd",
                    subLabel = "+1 WIDE",
                    modifier = Modifier.weight(1f),
                    backgroundColor = CricketExtraAmber,
                    textColor = Color.White,
                    onClick = onWide
                )

                ScoringButton(
                    label = "Nb",
                    subLabel = "+1 NO-BALL",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFEA580C),
                    textColor = Color.White,
                    onClick = onNoBall
                )
            }

            // Row 3: Wicket, Extras, and Undo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWicketClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CricketWicketRed,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsCricket,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OUT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onExtrasClick,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CricketExtraAmber,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Extras...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = onUndoClick,
                    enabled = canUndo,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Undo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 4: End Innings / Declare Option
            OutlinedButton(
                onClick = onEndInningsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (inningsNumber == 1) "End / Declare 1st Innings" else "End Match",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ScoringButton(
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
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = subLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.9f),
                letterSpacing = 0.4.sp
            )
        }
    }
}
