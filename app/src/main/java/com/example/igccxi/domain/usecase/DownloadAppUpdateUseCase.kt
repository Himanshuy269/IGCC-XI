package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.UpdateDownloadState
import com.example.igccxi.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.Flow

class DownloadAppUpdateUseCase(
    private val repository: AppUpdateRepository
) {
    operator fun invoke(downloadUrl: String): Flow<UpdateDownloadState> {
        return repository.downloadUpdateApk(downloadUrl)
    }
}
