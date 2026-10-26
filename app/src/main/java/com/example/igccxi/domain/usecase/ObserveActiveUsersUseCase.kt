package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.repository.PresenceRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveUsersUseCase(
    private val presenceRepository: PresenceRepository
) {
    operator fun invoke(): Flow<Int> = presenceRepository.observeActiveUsersCount()

    suspend fun recordHeartbeat(userId: String) {
        if (userId.isNotBlank()) {
            presenceRepository.recordHeartbeat(userId)
        }
    }
}
