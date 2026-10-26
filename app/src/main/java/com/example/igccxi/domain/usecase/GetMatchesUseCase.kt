package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase for streaming all cricket matches in real-time.
 */
class GetMatchesUseCase(
    private val repository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> {
        return repository.observeMatches()
    }
}
