package com.example.igccxi.screens.updater

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketSuccessGreen

/**
 * Dedicated In-App Update section card providing:
 * - Current version and build metadata
 * - Official GitHub Releases update source status
 * - Interactive 1-tap "Check for Updates" trigger
 * - Direct download and install trigger when a newer GitHub release is detected
 */
@Composable
fun AppUpdateSection(
    uiState: AppUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdateDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (uiState.isUpdateAvailable) CricketGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title, Icon & Source Channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.isUpdateAvailable) CricketGold.copy(alpha = 0.16f) else CricketGreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = if (uiState.isUpdateAvailable) CricketGold else CricketGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "App Version & Updates",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Source: GitHub (Himanshuy269/IGCC-XI)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                if (uiState.isChecking) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Checking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (uiState.isUpdateAvailable) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketGold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Update Available",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketSuccessGreen.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CricketSuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Up to Date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketSuccessGreen
                            )
                        }
                    }
                }
            }

            // Current Version & Metadata Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Installed Version",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "v${uiState.currentVersionName} (Build ${uiState.currentVersionCode})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Last Checked",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.lastCheckedTime ?: "Just now",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // If an update is detected from GitHub: Show new release highlights
            AnimatedVisibility(visible = uiState.isUpdateAvailable && uiState.updateInfo != null) {
                val update = uiState.updateInfo
                if (update != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CricketGold.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, CricketGold.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New GitHub Release: v${update.latestVersionName}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (update.fileSizeBytes > 0) {
                                val mb = String.format("%.1f", update.fileSizeBytes.toDouble() / (1024 * 1024))
                                Text(
                                    text = "~$mb MB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CricketGold
                                )
                            }
                        }

                        if (update.releaseNotes.isNotBlank()) {
                            Text(
                                text = update.releaseNotes.lines().take(3).joinToString("\n"),
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onOpenUpdateDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CricketGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Install Update (v${update.latestVersionName})",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Interactive Buttons Row: Check for Updates & View on GitHub
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCheckForUpdates,
                    enabled = !uiState.isChecking,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (uiState.isChecking) Modifier.rotate(rotation) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isChecking) "Checking..." else "Check for Updates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Himanshuy269/IGCC-XI/releases")
                        )
                        context.startActivity(browserIntent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "GitHub Releases",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GitHub",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
