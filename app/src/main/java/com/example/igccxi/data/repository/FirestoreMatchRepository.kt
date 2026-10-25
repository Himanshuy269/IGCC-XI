package com.example.igccxi.data.repository

import com.example.igccxi.data.model.Match
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

    override fun observeMatches(): Flow<List<Match>> = callbackFlow {
        val listenerRegistration = matchesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val matches = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Match::class.java)?.copy(id = doc.id)
                    }
                    trySend(matches)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override fun observeMatch(matchId: String): Flow<Match?> = callbackFlow {
        val listenerRegistration = matchesCollection.document(matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val match = snapshot.toObject(Match::class.java)?.copy(id = snapshot.id)
                    trySend(match)
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
