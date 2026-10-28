package com.example.igccxi.data.repository

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : MatchRepository {

    init {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (_: Exception) {
            // Settings already set by framework
        }
    }

    private val matchesCollection = firestore.collection("matches")

    companion object {
        // App Data Reset Epoch: all test match scorecards created prior to this timestamp are permanently purged & reset.
        const val DATA_RESET_EPOCH_MS = 1793136000000L

        val PURGED_MATCH_IDS = setOf(
            "F2A7oeEcJkZ6ZS2z4aoF",
            "BqSQumBqWYACyUNqUejC",
            "ByHqjCGGs6PIMoAJeMM8",
            "bas5xgJbDcttspMMANS6",
            "bs6wIYJzSaCKcbe2x0gt",
            "l2g2C6DZc4nkbqcqM2h2",
            "sRS9jGq0kh6jZpETkHTO",
            "zWfTJTNutdURFHKEmrjh"
        )

        fun isPurgedMatch(match: Match): Boolean {
            return match.createdAt < DATA_RESET_EPOCH_MS ||
                    match.id in PURGED_MATCH_IDS ||
                    (match.teamA.trim().equals("123", ignoreCase = true) && match.teamB.trim().equals("456", ignoreCase = true))
        }
    }

    override fun observeMatches(): Flow<List<Match>> = callbackFlow {
        val listenerRegistration = matchesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val uid = auth.currentUser?.uid ?: ""
                    val matches = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Match::class.java)?.copy(id = doc.id)
                    }.filterNot { match ->
                        val shouldPurge = isPurgedMatch(match)
                        if (shouldPurge && uid.isNotEmpty() && (match.scorerId == uid || match.scorerId.isEmpty())) {
                            try {
                                matchesCollection.document(match.id).delete()
                            } catch (_: Exception) {}
                        }
                        shouldPurge
                    }
                    trySend(matches)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override fun observeMatch(matchId: String): Flow<Match?> = callbackFlow {
        if (matchId in PURGED_MATCH_IDS) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listenerRegistration = matchesCollection.document(matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val match = snapshot.toObject(Match::class.java)?.copy(id = snapshot.id)
                    if (match != null && isPurgedMatch(match)) {
                        trySend(null)
                    } else {
                        trySend(match)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun createMatch(match: Match): String {
        val uid = getCurrentUserId()
        val docRef = if (match.id.isNotEmpty()) {
            matchesCollection.document(match.id)
        } else {
            matchesCollection.document()
        }

        val matchToSave = match.copy(
            id = docRef.id,
            scorerId = uid
        )

        docRef.set(matchToSave).await()
        return docRef.id
    }

    override suspend fun updateMatch(match: Match) {
        if (match.id.isEmpty()) return
        matchesCollection.document(match.id).set(match).await()
    }

    override suspend fun deleteMatch(matchId: String) {
        if (matchId.isEmpty()) return
        matchesCollection.document(matchId).delete().await()
    }

    override suspend fun getCurrentUserId(): String {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            currentUser.uid
        } else {
            try {
                val result = auth.signInAnonymously().await()
                result.user?.uid ?: "user_${System.currentTimeMillis()}"
            } catch (e: Exception) {
                "offline_scorer"
            }
        }
    }
}
