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
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                FirestoreMatchRepository()
            } else {
                InMemoryMatchRepository()
            }
        } catch (e: Exception) {
            InMemoryMatchRepository()
        }
    }

    // For testing or manual toggling
    fun setRepository(repository: MatchRepository) {
        synchronized(this) {
            repositoryInstance = repository
        }
    }
}
