package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.repository.MatchRepository

/**
 * Clean Architecture Use Case:
 * Permanently deletes a cricket match and its scorecard from the persistent repository.
 */
class DeleteMatchUseCase(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(matchId: String) {
        repository.deleteMatch(matchId)
    }
}
