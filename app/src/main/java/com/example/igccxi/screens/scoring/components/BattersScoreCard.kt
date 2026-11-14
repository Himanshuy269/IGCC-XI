package com.example.igccxi.screens.scoring.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.BatterScore
import com.example.igccxi.ui.components.CommonPlayerBadge
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketWicketContainer
import com.example.igccxi.ui.theme.CricketWicketRed
import com.example.igccxi.domain.model.PlayerProfile
import com.example.igccxi.ui.components.PlayerAvatar

@Composable
fun BattersScoreCard(
    striker: BatterScore?,
    nonStriker: BatterScore?,
    lastOutBatter: BatterScore? = null,
    onSwapStrike: () -> Unit,
    isCommonPlayer: (String?) -> Boolean = { false },
    onEditStriker: (() -> Unit)? = null,
    onEditNonStriker: (() -> Unit)? = null,
    allProfiles: List<PlayerProfile> = emptyList(),
    onProfileClick: ((String) -> Unit)? = null
) {
    val isLoneBatsman = nonStriker == null || nonStriker.id.isBlank()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Swap Ends Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoneBatsman) "BATTER AT CREASE (LONE)" else "BATTERS AT CREASE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )

                if (!isLoneBatsman) {
                    Surface(
                        onClick = onSwapStrike,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Strike",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Swap Strike",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketGreenPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CricketGreenPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "âš¡ Batting Alone",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CricketGreenPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BATTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.8f)
                )
                Text(
                    text = "R",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "B",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "4s",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    text = "6s",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f)
                )
                Text(
                    text = "SR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Striker Row (Highlighted with On-Strike indicator)
            val strikerRawName = striker?.name ?: "Striker"
            val strikerProfile = allProfiles.find { it.name.equals(strikerRawName, ignoreCase = true) }
            BatterTableRow(
                name = if (isLoneBatsman) "$strikerRawName (Lone)" else strikerRawName,
                runs = striker?.runs ?: 0,
                balls = striker?.ballsFaced ?: 0,
                fours = striker?.fours ?: 0,
                sixes = striker?.sixes ?: 0,
                strikeRate = striker?.strikeRateFormatted ?: "0.0",
                isOnStrike = true,
                isCommonPlayer = isCommonPlayer(striker?.name),
                profile = strikerProfile,
                onProfileClick = if (onProfileClick != null && striker?.name?.isNotBlank() == true) { { onProfileClick(strikerRawName) } } else null,
                onEditClick = onEditStriker
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Non-Striker Row or Lone Batsman Notice
            if (isLoneBatsman) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Gully Cricket Rule: Batsman is batting alone (faces every ball).",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val nonStrikerRawName = nonStriker?.name ?: "Non-Striker"
                val nonStrikerProfile = allProfiles.find { it.name.equals(nonStrikerRawName, ignoreCase = true) }
                BatterTableRow(
                    name = nonStrikerRawName,
                    runs = nonStriker?.runs ?: 0,
                    balls = nonStriker?.ballsFaced ?: 0,
                    fours = nonStriker?.fours ?: 0,
                    sixes = nonStriker?.sixes ?: 0,
                    strikeRate = nonStriker?.strikeRateFormatted ?: "0.0",
                    isOnStrike = false,
                    isCommonPlayer = isCommonPlayer(nonStriker?.name),
                    profile = nonStrikerProfile,
                    onProfileClick = if (onProfileClick != null && nonStriker?.name?.isNotBlank() == true) { { onProfileClick(nonStrikerRawName) } } else null,
                    onEditClick = onEditNonStriker
                )
            }

            if (lastOutBatter != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CricketWicketContainer.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CricketWicketRed,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "OUT",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                    if (isCommonPlayer(lastOutBatter.name)) {
                        CommonPlayerBadge(modifier = Modifier.padding(end = 6.dp))
                    }
                    Text(
                        text = "${lastOutBatter.name}  ${lastOutBatter.runs}(${lastOutBatter.ballsFaced})  â€¢  ${lastOutBatter.dismissal}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BatterTableRow(
    name: String,
    runs: Int,
    balls: Int,
    fours: Int,
    sixes: Int,
    strikeRate: String,
    isOnStrike: Boolean,
    isCommonPlayer: Boolean = false,
    profile: PlayerProfile? = null,
    onProfileClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isOnStrike) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (name.isNotBlank()) {
                    PlayerAvatar(
                        name = name.removeSuffix(" (Lone)"),
                        photoPath = profile?.photoPath,
                        avatarIcon = profile?.avatarIcon,
                        colorHex = profile?.avatarColorHex ?: 0xFF1B5E20,
                        size = 22.dp,
                        onClick = onProfileClick
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                if (isOnStrike) {
                    Text(
                        text = "ðŸ",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = name + if (isOnStrike) " *" else "",
                    fontSize = 13.sp,
                    fontWeight = if (isOnStrike) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isOnStrike) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCommonPlayer) {
                    Spacer(modifier = Modifier.width(4.dp))
                    CommonPlayerBadge()
                }
                if (onEditClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        onClick = onEditClick,
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit name",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "$runs",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (isOnStrike) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )
            Text(
                text = "$balls",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )
            Text(
                text = "$fours",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
            Text(
                text = "$sixes",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
            Text(
                text = strikeRate,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f)
            )
        }
    }
}
