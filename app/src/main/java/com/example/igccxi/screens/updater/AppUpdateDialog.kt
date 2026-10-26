package com.example.igccxi.screens.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.model.UpdateDownloadState
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketSuccessGreen
import com.example.igccxi.ui.theme.CricketWicketRed
import java.io.File

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    currentVersionName: String,
    isMandatory: Boolean,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isMandatory && downloadState !is UpdateDownloadState.Downloading) {
                onDismiss()
            }
        },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = CricketGreenPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = CricketGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "App Update Available",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isMandatory) "Mandatory version update" else "New features & optimizations",
                        fontSize = 12.sp,
                        color = if (isMandatory) CricketWicketRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isMandatory) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Version comparison badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Current", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v$currentVersionName", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("➔", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketGold.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CricketGold.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Latest", fontSize = 11.sp, color = CricketGold, fontWeight = FontWeight.Bold)
                            Text("v${updateInfo.latestVersionName}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Release Notes Card
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "What's New:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateInfo.releaseNotes,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Download / Installation Progress state
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        if (updateInfo.fileSizeBytes > 0) {
                            val mb = String.format(java.util.Locale.US, "%.1f", updateInfo.fileSizeBytes.toDouble() / (1024 * 1024))
                            Text(
                                text = "Download package size: ~$mb MB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is UpdateDownloadState.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading Update...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${downloadState.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { downloadState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CricketGreenPrimary
                            )

                            if (downloadState.totalBytes > 0) {
                                val dlMb = String.format(java.util.Locale.US, "%.1f", downloadState.bytesDownloaded.toDouble() / (1024 * 1024))
                                val totalMb = String.format(java.util.Locale.US, "%.1f", downloadState.totalBytes.toDouble() / (1024 * 1024))
                                Text(
                                    text = "$dlMb MB / $totalMb MB",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    is UpdateDownloadState.ReadyToInstall -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CricketSuccessGreen.copy(alpha = 0.14f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CricketSuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download complete! Ready to install.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketSuccessGreen
                            )
                        }
                    }
                    is UpdateDownloadState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CricketWicketRed.copy(alpha = 0.14f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = CricketWicketRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = downloadState.message,
                                fontSize = 11.sp,
                                color = CricketWicketRed,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (downloadState) {
                is UpdateDownloadState.Idle -> {
                    Button(
                        onClick = onStartDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Update", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateDownloadState.Downloading -> {
                    // Disabled while downloading
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Downloading (${downloadState.progressPercent}%)...")
                    }
                }
                is UpdateDownloadState.ReadyToInstall -> {
                    Button(
                        onClick = { onInstallApk(downloadState.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = CricketSuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install Update Now", fontWeight = FontWeight.ExtraBold)
                    }
                }
                is UpdateDownloadState.Error -> {
                    Button(
                        onClick = onStartDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Download", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (!isMandatory && downloadState !is UpdateDownloadState.Downloading) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

@Composable
fun InstallPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Permission Required", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Text(
                text = "Android requires permission to install APK updates directly from the app. Please enable 'Allow from this source' for IGCC XI in Android Settings, then return here to complete the installation.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CricketGreenPrimary)
            ) {
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
