package com.example.igccxi.ui.components

import androidx.compose.foundation.background
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
import com.example.igccxi.data.model.BallEvent
import com.example.igccxi.data.model.BallType

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
            Text(
                text = "Over in progress...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            recentBalls.takeLast(18).forEach { ball ->
                BallChip(ball = ball)
            }
        }
    }
}

@Composable
fun BallChip(ball: BallEvent) {
    val (bgColor, textColor) = when {
        ball.isWicket -> Color(0xFFD32F2F) to Color.White
        ball.runsOffBat == 6 -> Color(0xFF7B1FA2) to Color.White
        ball.runsOffBat == 4 -> Color(0xFF1976D2) to Color.White
        ball.type == BallType.WIDE || ball.type == BallType.NO_BALL -> Color(0xFFFFA000) to Color.Black
        ball.runsOffBat == 0 -> Color(0xFFEEEEEE) to Color(0xFF616161)
        else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
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
            fontWeight = FontWeight.Bold
        )
    }
}
