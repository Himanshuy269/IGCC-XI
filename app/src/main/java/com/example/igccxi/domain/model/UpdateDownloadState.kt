package com.example.igccxi.domain.model

import java.io.File

/**
 * Represents the progressive lifecycle states when downloading and preparing an update APK.
 */
sealed class UpdateDownloadState {
    data object Idle : UpdateDownloadState()
    
    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateDownloadState()

    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()

    data class Error(val message: String) : UpdateDownloadState()
}
