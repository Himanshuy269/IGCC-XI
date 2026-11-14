package com.example.igccxi.screens.scoring.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.BowlerScore
import com.example.igccxi.domain.model.PlayerProfile
import com.example.igccxi.ui.components.CommonPlayerBadge
import com.example.igccxi.ui.components.PlayerAvatar
import com.example.igccxi.ui.theme.CricketWicketRed

@Composable
fun BowlerScoreCard(
    bowler: BowlerScore?,
    onChangeBowlerClick: () -> Unit,
    isCommonPlayer: Boolean = false,
    onEditBowler: (() -> Unit)? = null,
    allProfiles: List<PlayerProfile> = emptyList(),
    onProfileClick: ((String) -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(
                        text = "BOWLER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val bowlerRawName = bowler?.name ?: "Current Bowler"
                    val bowlerProfile = allProfiles.find { it.name.equals(bowlerRawName, ignoreCase = true) }
                    if (bowler != null && bowler.name.isNotBlank()) {
                        PlayerAvatar(
                            name = bowlerRawName,
                            photoPath = bowlerProfile?.photoPath,
                            avatarIcon = bowlerProfile?.avatarIcon,
                            colorHex = bowlerProfile?.avatarColorHex ?: 0xFF1B5E20,
                            size = 22.dp,
                            onClick = if (onProfileClick != null) { { onProfileClick(bowlerRawName) } } else null
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = bowlerRawName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCommonPlayer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CommonPlayerBadge()
                    }
                    if (onEditBowler != null && bowler != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            onClick = onEditBowler,
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Bowler Name",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    onClick = onChangeBowlerClick,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChangeCircle,
                            contentDescription = "Change Bowler",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bowler Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BowlerStatBadge(label = "OVERS", value = bowler?.oversFormatted ?: "0.0")
                BowlerStatBadge(label = "MAIDENS", value = "${bowler?.maidens ?: 0}")
                BowlerStatBadge(label = "RUNS", value = "${bowler?.runsConceded ?: 0}")
                BowlerStatBadge(label = "WICKETS", value = "${bowler?.wickets ?: 0}", isWicket = true)
                BowlerStatBadge(label = "ECON", value = bowler?.economyFormatted ?: "0.0")
            }
        }
    }
}

@Composable
fun BowlerStatBadge(label: String, value: String, isWicket: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = if (isWicket && value != "0") CricketWicketRed else MaterialTheme.colorScheme.onSurface
        )
    }
}
