package com.example.igccxi.domain.repository

import com.example.igccxi.domain.model.Match
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining operations for observing and mutating cricket matches.
 * Independent of any concrete data source (Firestore, memory, cache).
 */
interface MatchRepository {
    /**
     * Real-time stream of all public matches, updated whenever any score changes.
     */
    fun observeMatches(): Flow<List<Match>>

    /**
     * Real-time stream of a specific match's live scorecard.
     */
    fun observeMatch(matchId: String): Flow<Match?>

    /**
     * Creates a new match in the persistent store and returns the generated match ID.
     */
    suspend fun createMatch(match: Match): String

    /**
     * Updates an existing match (runs, wickets, overs, status, etc.).
     */
    suspend fun updateMatch(match: Match)

    /**
     * Deletes a match and its live scorecard from the persistent store.
     */
    suspend fun deleteMatch(matchId: String)

    /**
     * Retrieves the current user's unique identifier for scorer ownership checks.
     */
    suspend fun getCurrentUserId(): String
}
