package com.example.igccxi.screens.updater

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.model.UpdateDownloadState
import com.example.igccxi.domain.usecase.CheckForAppUpdateUseCase
import com.example.igccxi.domain.usecase.DownloadAppUpdateUseCase
import com.example.igccxi.domain.usecase.InstallAppUpdateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class AppUpdateViewModel @JvmOverloads constructor(
    application: Application,
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase = RepositoryProvider.provideCheckForAppUpdateUseCase(application),
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = RepositoryProvider.provideDownloadAppUpdateUseCase(application),
    private val installAppUpdateUseCase: InstallAppUpdateUseCase = RepositoryProvider.provideInstallAppUpdateUseCase(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    init {
        // Automatic silent check on initialization
        checkForUpdate(isManual = false)
    }

    fun checkForUpdate(isManual: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, feedbackMessage = null) }
            val timeString = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            try {
                val result = checkForAppUpdateUseCase()
                _uiState.update { current ->
                    current.copy(
                        isChecking = false,
                        lastCheckedTime = timeString,
                        isUpdateAvailable = result.isUpdateAvailable,
                        currentVersionCode = result.currentVersionCode,
                        currentVersionName = result.currentVersionName,
                        updateInfo = result.updateInfo,
                        isMandatory = result.isMandatory,
                        showUpdateDialog = result.isUpdateAvailable,
                        feedbackMessage = if (isManual) {
                            if (result.isUpdateAvailable) {
                                null
                            } else if (result.updateInfo == null) {
                                "Could not connect to update servers. Please check your internet connection."
                            } else {
                                "You are on the latest version of IGCC XI (v${result.currentVersionName})! 🎉"
                            }
                        } else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        lastCheckedTime = timeString,
                        feedbackMessage = if (isManual) "Unable to check for updates: ${e.message}" else null
                    )
                }
            }
        }
    }

    fun startDownload() {
        val updateInfo = _uiState.value.updateInfo ?: return
        val url = updateInfo.downloadUrl
        if (url.isBlank()) {
            _uiState.update {
                it.copy(downloadState = UpdateDownloadState.Error("No download URL provided in update manifest."))
            }
            return
        }

        viewModelScope.launch {
            downloadAppUpdateUseCase(url).collect { state ->
                _uiState.update { it.copy(downloadState = state) }
            }
        }
    }

    fun installDownloadedApk(apkFile: File) {
        val canInstall = installAppUpdateUseCase.canRequestPackageInstalls()
        if (!canInstall) {
            _uiState.update { it.copy(showPermissionDialog = true) }
            return
        }

        val result = installAppUpdateUseCase.installApk(apkFile)
        result.onFailure { error ->
            _uiState.update {
                it.copy(
                    downloadState = UpdateDownloadState.Error("Failed to launch installer: ${error.message}")
                )
            }
        }
    }

    fun openInstallPermissionSettings() {
        _uiState.update { it.copy(showPermissionDialog = false) }
        installAppUpdateUseCase.launchInstallPermissionSettings()
    }

    fun dismissPermissionDialog() {
        _uiState.update { it.copy(showPermissionDialog = false) }
    }

    fun dismissUpdateDialog() {
        if (!_uiState.value.isMandatory) {
            _uiState.update { it.copy(showUpdateDialog = false) }
        }
    }

    fun openUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = true) }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
