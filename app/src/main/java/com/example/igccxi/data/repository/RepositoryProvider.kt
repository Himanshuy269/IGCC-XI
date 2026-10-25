package com.example.igccxi.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp

object RepositoryProvider {

    @Volatile
    private var repositoryInstance: MatchRepository? = null

    fun getRepository(context: Context): MatchRepository {
        return repositoryInstance ?: synchronized(this) {
            repositoryInstance ?: createRepository(context.applicationContext).also {
                repositoryInstance = it
            }
        }
    }

    private fun createRepository(context: Context): MatchRepository {
        return try {
            val app = if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                FirebaseApp.initializeApp(context)
            }
            if (app != null) {
                FirestoreMatchRepository()
            } else {
                InMemoryMatchRepository(context)
            }
        } catch (e: Exception) {
            InMemoryMatchRepository(context)
        }
    }

    // For testing or manual toggling
    fun setRepository(repository: MatchRepository) {
        synchronized(this) {
            repositoryInstance = repository
        }
    }
}
