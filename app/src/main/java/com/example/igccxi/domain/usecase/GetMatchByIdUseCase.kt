package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase for streaming a specific match by ID in real-time.
 */
class GetMatchByIdUseCase(
    private val repository: MatchRepository
) {
    operator fun invoke(matchId: String): Flow<Match?> {
        return repository.observeMatch(matchId)
    }
}
