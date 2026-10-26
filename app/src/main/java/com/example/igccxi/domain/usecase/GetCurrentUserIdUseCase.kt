package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.repository.MatchRepository

/**
 * UseCase for obtaining the current authenticated or anonymous user ID.
 */
class GetCurrentUserIdUseCase(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(): String {
        return repository.getCurrentUserId()
    }
}
