package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository

/**
 * UseCase for updating live scorecard data for a match.
 */
class UpdateMatchUseCase(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(match: Match) {
        repository.updateMatch(match)
    }
}
