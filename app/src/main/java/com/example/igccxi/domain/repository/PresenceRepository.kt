package com.example.igccxi.domain.repository

import kotlinx.coroutines.flow.Flow

interface PresenceRepository {
    /**
     * Updates presence / heartbeat for the current user in real-time.
     */
    suspend fun recordHeartbeat(userId: String)

    /**
     * Observes the current count of active users within the active window (e.g. past 5 minutes).
     */
    fun observeActiveUsersCount(): Flow<Int>
}
