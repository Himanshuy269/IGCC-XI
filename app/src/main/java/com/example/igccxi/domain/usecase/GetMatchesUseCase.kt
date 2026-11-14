package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * ====================================================================================
 * CLEAN ARCHITECTURE: USE CASE LAYER
 * ====================================================================================
 *
 * [GetMatchesUseCase] streams cricket matches reactively in real-time from [MatchRepository].
 *
 * JUNIOR DEVELOPER NOTE:
 * - Following Clean Architecture, screens and ViewModels do NOT query Firestore or SQL directly.
 * - Instead, they call a focused, single-responsibility UseCase like this one.
 * - This allows changing the underlying database without rewriting UI or ViewModel logic.
 */
class GetMatchesUseCase(
    private val repository: MatchRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    operator fun invoke(): Flow<List<Match>> {
        return repository.observeMatches().flowOn(defaultDispatcher)
    }
}
