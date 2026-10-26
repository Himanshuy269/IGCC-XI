package com.example.igccxi.data.repository

import android.util.Log
import com.example.igccxi.domain.model.UserPresence
import com.example.igccxi.domain.repository.PresenceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestorePresenceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PresenceRepository {

    companion object {
        private const val TAG = "FirestorePresence"
        private const val COLLECTION_NAME = "active_users"
        private const val ACTIVE_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val presenceCollection = firestore.collection(COLLECTION_NAME)

    override suspend fun recordHeartbeat(userId: String) {
        if (userId.isBlank()) return
        try {
            val presence = UserPresence(
                uid = userId,
                lastActiveAt = System.currentTimeMillis(),
                appVersion = "1.2.4",
                platform = "Android"
            )
            presenceCollection.document(userId).set(presence).await()
            Log.d(TAG, "Recorded active heartbeat for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record heartbeat: ${e.message}")
        }
    }

    override fun observeActiveUsersCount(): Flow<Int> = callbackFlow {
        // Fallback default
        trySend(1)

        val listenerRegistration = presenceCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Presence listener error: ${error.message}")
                trySend(1)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val cutoff = System.currentTimeMillis() - ACTIVE_THRESHOLD_MS
                val activeCount = snapshot.documents.count { doc ->
                    val lastActive = doc.getLong("lastActiveAt") ?: 0L
                    lastActive >= cutoff
                }
                // Always at least 1 active user (the current device)
                trySend(maxOf(activeCount, 1))
            } else {
                trySend(1)
            }
        }

        // Periodic ticker to prune expired users locally even without fresh remote writes
        val tickerJob = launch {
            while (isActive) {
                delay(30_000L) // every 30 seconds
                try {
                    val snapshot = presenceCollection.get().await()
                    val cutoff = System.currentTimeMillis() - ACTIVE_THRESHOLD_MS
                    val activeCount = snapshot.documents.count { doc ->
                        val lastActive = doc.getLong("lastActiveAt") ?: 0L
                        lastActive >= cutoff
                    }
                    trySend(maxOf(activeCount, 1))
                } catch (_: Exception) {
                    // Ignore transient network errors on background tick
                }
            }
        }

        awaitClose {
            listenerRegistration.remove()
            tickerJob.cancel()
        }
    }
}

class InMemoryPresenceRepository : PresenceRepository {
    override suspend fun recordHeartbeat(userId: String) {
        // No-op for in-memory
    }

    override fun observeActiveUsersCount(): Flow<Int> = flow {
        emit(1)
    }
}
