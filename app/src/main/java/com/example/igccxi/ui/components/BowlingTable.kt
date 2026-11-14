package com.example.igccxi.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.PlayerProfile
import com.example.igccxi.ui.components.CommonPlayerBadge
import com.example.igccxi.ui.components.PlayerAvatar

@Composable
fun BowlingTable(
    innings: Innings,
    modifier: Modifier = Modifier,
    isCommonPlayer: (String?) -> Boolean = { false },
    allProfiles: List<PlayerProfile> = emptyList(),
    onProfileClick: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val displayTitle = if (innings.bowlingTeam.trim().length > 1) "${innings.bowlingTeam.trim()} Bowling" else "Bowling"
            Text(
                text = displayTitle,
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
                    text = "Bowler",
                    modifier = Modifier.weight(2.5f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "O",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "M",
                    modifier = Modifier.weight(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "R",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "W",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ECON",
                    modifier = Modifier.weight(1.0f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bowler Rows
            innings.bowlers.forEach { bowler ->
                val isCurrentBowler = bowler.id == innings.currentBowlerId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prof = allProfiles.find { it.name.equals(bowler.name, ignoreCase = true) }
                        PlayerAvatar(
                            name = bowler.name,
                            photoPath = prof?.photoPath,
                            avatarIcon = prof?.avatarIcon,
                            colorHex = prof?.avatarColorHex ?: 0xFF1B5E20,
                            size = 20.dp,
                            onClick = if (onProfileClick != null) { { onProfileClick(bowler.name) } } else null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCurrentBowler) "${bowler.name} â€¢" else bowler.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrentBowler) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isCommonPlayer(bowler.name)) {
                            Spacer(modifier = Modifier.width(4.dp))
                            CommonPlayerBadge()
                        }
                    }
                    Text(
                        text = bowler.oversFormatted,
                        modifier = Modifier.weight(0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${bowler.maidens}",
                        modifier = Modifier.weight(0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${bowler.runsConceded}",
                        modifier = Modifier.weight(0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${bowler.wickets}",
                        modifier = Modifier.weight(0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bowler.economyFormatted,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}
