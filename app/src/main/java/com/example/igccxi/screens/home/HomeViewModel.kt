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

/**
 * ViewModel for the Home screen, cleanly consuming domain UseCases.
 */
class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchesUseCase: GetMatchesUseCase = RepositoryProvider.provideGetMatchesUseCase(application),
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = RepositoryProvider.provideGetCurrentUserIdUseCase(application)
) : AndroidViewModel(application) {

    private val _userId = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                _userId.value = getCurrentUserIdUseCase()
            } catch (e: Exception) {
                _error.value = "Failed to initialize identity: ${e.message}"
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getMatchesUseCase().catch { e -> _error.value = e.message },
        _userId,
        _error
    ) { matches, uid, err ->
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
            isLoading = false,
            errorMessage = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )
}
