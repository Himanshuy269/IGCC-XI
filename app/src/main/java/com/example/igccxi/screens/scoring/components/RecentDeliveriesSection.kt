package com.example.igccxi.screens.scoring.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.BallEvent
import com.example.igccxi.ui.components.BallByBallTicker

@Composable
fun RecentDeliveriesSection(recentBalls: List<BallEvent>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT DELIVERIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            if (recentBalls.isNotEmpty()) {
                Text(
                    text = "Last ${recentBalls.size.coerceAtMost(12)} balls",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        BallByBallTicker(recentBalls = recentBalls)
    }
}
