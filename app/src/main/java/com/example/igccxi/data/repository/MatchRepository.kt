package com.example.igccxi.data.repository

import com.example.igccxi.data.model.Match
import kotlinx.coroutines.flow.Flow

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
     * Creates a new match in the database and returns the generated match ID.
     */
    suspend fun createMatch(match: Match): String

    /**
     * Updates an existing match (runs, wickets, overs, status, etc.).
     */
    suspend fun updateMatch(match: Match)

    /**
     * Retrieves the current user's unique identifier for scorer ownership checks.
     */
    suspend fun getCurrentUserId(): String
}
