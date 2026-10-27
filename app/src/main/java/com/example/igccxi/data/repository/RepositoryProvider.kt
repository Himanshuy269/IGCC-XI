package com.example.igccxi.data.repository

import android.content.Context
import com.example.igccxi.domain.repository.MatchRepository
import com.example.igccxi.domain.usecase.CreateMatchUseCase
import com.example.igccxi.domain.usecase.ExportScorecardPdfUseCase
import com.example.igccxi.domain.usecase.GetCurrentUserIdUseCase
import com.example.igccxi.domain.usecase.GetMatchByIdUseCase
import com.example.igccxi.domain.usecase.GetMatchesUseCase
import com.example.igccxi.domain.usecase.UpdateMatchUseCase
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

    // UseCase factories for Clean Architecture
    fun provideGetMatchesUseCase(context: Context): GetMatchesUseCase =
        GetMatchesUseCase(getRepository(context))

    fun provideGetMatchByIdUseCase(context: Context): GetMatchByIdUseCase =
        GetMatchByIdUseCase(getRepository(context))

    fun provideCreateMatchUseCase(context: Context): CreateMatchUseCase =
        CreateMatchUseCase(getRepository(context))

    fun provideUpdateMatchUseCase(context: Context): UpdateMatchUseCase =
        UpdateMatchUseCase(getRepository(context))

    fun provideDeleteMatchUseCase(context: Context): com.example.igccxi.domain.usecase.DeleteMatchUseCase =
        com.example.igccxi.domain.usecase.DeleteMatchUseCase(getRepository(context))

    fun provideGetCurrentUserIdUseCase(context: Context): GetCurrentUserIdUseCase =
        GetCurrentUserIdUseCase(getRepository(context))

    fun provideExportScorecardPdfUseCase(): ExportScorecardPdfUseCase =
        ExportScorecardPdfUseCase()

    fun provideGetPlayerRankingsUseCase(context: Context): com.example.igccxi.domain.usecase.GetPlayerRankingsUseCase =
        com.example.igccxi.domain.usecase.GetPlayerRankingsUseCase(getRepository(context))

    @Volatile
    private var appUpdateRepositoryInstance: com.example.igccxi.domain.repository.AppUpdateRepository? = null

    fun getAppUpdateRepository(context: Context): com.example.igccxi.domain.repository.AppUpdateRepository {
        return appUpdateRepositoryInstance ?: synchronized(this) {
            appUpdateRepositoryInstance ?: com.example.igccxi.data.updater.AppUpdateManager(context.applicationContext).also {
                appUpdateRepositoryInstance = it
            }
        }
    }

    fun provideCheckForAppUpdateUseCase(context: Context): com.example.igccxi.domain.usecase.CheckForAppUpdateUseCase =
        com.example.igccxi.domain.usecase.CheckForAppUpdateUseCase(getAppUpdateRepository(context))

    fun provideDownloadAppUpdateUseCase(context: Context): com.example.igccxi.domain.usecase.DownloadAppUpdateUseCase =
        com.example.igccxi.domain.usecase.DownloadAppUpdateUseCase(getAppUpdateRepository(context))

    fun provideInstallAppUpdateUseCase(context: Context): com.example.igccxi.domain.usecase.InstallAppUpdateUseCase =
        com.example.igccxi.domain.usecase.InstallAppUpdateUseCase(getAppUpdateRepository(context))

    @Volatile
    private var presenceRepositoryInstance: com.example.igccxi.domain.repository.PresenceRepository? = null

    fun getPresenceRepository(context: Context): com.example.igccxi.domain.repository.PresenceRepository {
        return presenceRepositoryInstance ?: synchronized(this) {
            presenceRepositoryInstance ?: createPresenceRepository(context.applicationContext).also {
                presenceRepositoryInstance = it
            }
        }
    }

    private fun createPresenceRepository(context: Context): com.example.igccxi.domain.repository.PresenceRepository {
        return try {
            val app = if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                FirebaseApp.initializeApp(context)
            }
            if (app != null) {
                FirestorePresenceRepository()
            } else {
                InMemoryPresenceRepository()
            }
        } catch (_: Exception) {
            InMemoryPresenceRepository()
        }
    }

    fun provideObserveActiveUsersUseCase(context: Context): com.example.igccxi.domain.usecase.ObserveActiveUsersUseCase =
        com.example.igccxi.domain.usecase.ObserveActiveUsersUseCase(getPresenceRepository(context))
}
