package com.example.igccxi.domain.repository

import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.model.UpdateDownloadState
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Domain contract for checking, downloading, and installing app updates.
 */
interface AppUpdateRepository {
    fun getCurrentVersionCode(): Int
    fun getCurrentVersionName(): String
    suspend fun fetchLatestUpdateInfo(): AppUpdateInfo?
    fun downloadUpdateApk(downloadUrl: String): Flow<UpdateDownloadState>
    fun canRequestPackageInstalls(): Boolean
    fun launchInstallPermissionSettings()
    fun installApk(apkFile: File): Result<Unit>
}
