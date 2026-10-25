package com.example.igccxi.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.model.MatchStatus
import com.example.igccxi.data.repository.MatchRepository
import com.example.igccxi.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val liveMatches: List<Match> = emptyList(),
    val recentMatches: List<Match> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    application: Application,
    private val repository: MatchRepository = RepositoryProvider.getRepository(application)
) : AndroidViewModel(application) {

    private val _userId = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                _userId.value = repository.getCurrentUserId()
            } catch (e: Exception) {
                _error.value = "Failed to initialize identity: ${e.message}"
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeMatches().catch { e -> _error.value = e.message },
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
