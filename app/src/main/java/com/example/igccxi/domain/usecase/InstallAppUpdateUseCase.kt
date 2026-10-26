package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.repository.AppUpdateRepository
import java.io.File

class InstallAppUpdateUseCase(
    private val repository: AppUpdateRepository
) {
    fun canRequestPackageInstalls(): Boolean = repository.canRequestPackageInstalls()

    fun launchInstallPermissionSettings() = repository.launchInstallPermissionSettings()

    fun installApk(apkFile: File): Result<Unit> = repository.installApk(apkFile)
}
