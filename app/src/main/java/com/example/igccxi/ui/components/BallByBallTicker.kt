package com.example.igccxi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.BallEvent
import com.example.igccxi.domain.model.BallType
import com.example.igccxi.ui.theme.CricketExtraAmber
import com.example.igccxi.ui.theme.CricketFourBlue
import com.example.igccxi.ui.theme.CricketSixPurple
import com.example.igccxi.ui.theme.CricketWicketRed

@Composable
fun BallByBallTicker(
    recentBalls: List<BallEvent>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(recentBalls.size) {
        if (recentBalls.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (recentBalls.isEmpty()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "New over starting...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else {
            recentBalls.takeLast(18).forEach { ball ->
                BallChip(ball = ball)
            }
        }
    }
}

@Composable
fun BallChip(ball: BallEvent) {
    val bgColor: Color
    val textColor: Color
    when {
        ball.isWicket -> {
            bgColor = CricketWicketRed
            textColor = Color.White
        }
        ball.runsOffBat == 6 -> {
            bgColor = CricketSixPurple
            textColor = Color.White
        }
        ball.runsOffBat == 4 -> {
            bgColor = CricketFourBlue
            textColor = Color.White
        }
        ball.type == BallType.WIDE || ball.type == BallType.NO_BALL -> {
            bgColor = CricketExtraAmber
            textColor = Color.White
        }
        ball.runsOffBat == 0 -> {
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
        else -> {
            bgColor = MaterialTheme.colorScheme.primaryContainer
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
        }
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball.displayText,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
