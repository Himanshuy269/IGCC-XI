package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.repository.AppUpdateRepository

data class UpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val currentVersionCode: Int,
    val currentVersionName: String,
    val updateInfo: AppUpdateInfo?,
    val isMandatory: Boolean
)

/**
 * Use case to determine if an update is published and available for the current installed app.
 */
class CheckForAppUpdateUseCase(
    private val repository: AppUpdateRepository
) {
    suspend operator fun invoke(): UpdateCheckResult {
        val currentCode = repository.getCurrentVersionCode()
        val currentName = repository.getCurrentVersionName()
        val updateInfo = repository.fetchLatestUpdateInfo()

        if (updateInfo == null) {
            return UpdateCheckResult(
                isUpdateAvailable = false,
                currentVersionCode = currentCode,
                currentVersionName = currentName,
                updateInfo = null,
                isMandatory = false
            )
        }

        val isNewer = updateInfo.latestVersionCode > currentCode
        val isMandatory = isNewer && (updateInfo.isMandatory || currentCode < updateInfo.minSupportedVersionCode)

        return UpdateCheckResult(
            isUpdateAvailable = isNewer,
            currentVersionCode = currentCode,
            currentVersionName = currentName,
            updateInfo = updateInfo,
            isMandatory = isMandatory
        )
    }
}
