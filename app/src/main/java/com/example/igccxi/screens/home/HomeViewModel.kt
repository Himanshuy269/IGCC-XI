package com.example.igccxi.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.domain.usecase.GetCurrentUserIdUseCase
import com.example.igccxi.domain.usecase.GetMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.igccxi.domain.usecase.ObserveActiveUsersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * ViewModel for the Home screen, cleanly consuming domain UseCases.
 */
class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchesUseCase: GetMatchesUseCase = RepositoryProvider.provideGetMatchesUseCase(application),
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = RepositoryProvider.provideGetCurrentUserIdUseCase(application),
    private val observeActiveUsersUseCase: ObserveActiveUsersUseCase = RepositoryProvider.provideObserveActiveUsersUseCase(application)
) : AndroidViewModel(application) {

    private val _userId = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                val uid = getCurrentUserIdUseCase()
                _userId.value = uid
                if (uid.isNotBlank()) {
                    observeActiveUsersUseCase.recordHeartbeat(uid)
                }
            } catch (e: Exception) {
                _error.value = "Failed to initialize identity: ${e.message}"
            }
        }

        // Periodic presence heartbeat every 60 seconds while viewing home
        viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                val uid = _userId.value
                if (uid.isNotBlank()) {
                    try {
                        observeActiveUsersUseCase.recordHeartbeat(uid)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getMatchesUseCase().catch { e -> _error.value = e.message },
        _userId,
        observeActiveUsersUseCase().catch { emit(1) },
        _error
    ) { matches, uid, activeUsers, err ->
        val live = matches.filter {
            it.status == MatchStatus.INNINGS_1 ||
            it.status == MatchStatus.INNINGS_BREAK ||
            it.status == MatchStatus.INNINGS_2
        }
        val completed = matches.filter { it.status == MatchStatus.COMPLETED }

        HomeUiState(
            liveMatches = live,
            recentMatches = completed,
            currentUserId = uid,
            activeUsersCount = maxOf(activeUsers, 1),
            isLoading = false,
            errorMessage = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )
}
