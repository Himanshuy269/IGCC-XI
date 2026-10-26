package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository

/**
 * UseCase for creating a new match in the repository.
 */
class CreateMatchUseCase(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(match: Match): String {
        return repository.createMatch(match)
    }
}
