package com.example.igccxi.screens.updater

import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.model.UpdateDownloadState

/**
 * UI State for the In-App Auto-Updater.
 */
data class AppUpdateUiState(
    val isChecking: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val currentVersionCode: Int = 1,
    val currentVersionName: String = "1.0",
    val updateInfo: AppUpdateInfo? = null,
    val isMandatory: Boolean = false,
    val downloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    val showUpdateDialog: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val feedbackMessage: String? = null,
    val lastCheckedTime: String? = null
)
