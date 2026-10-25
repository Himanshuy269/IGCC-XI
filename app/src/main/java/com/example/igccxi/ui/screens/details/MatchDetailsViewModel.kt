package com.example.igccxi.ui.screens.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.repository.MatchRepository
import com.example.igccxi.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchDetailsUiState(
    val match: Match? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MatchDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: MatchRepository = RepositoryProvider.getRepository(application)

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState: StateFlow<MatchDetailsUiState> = _uiState.asStateFlow()

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            try {
                val uid = repository.getCurrentUserId()
                _uiState.update { it.copy(currentUserId = uid) }

                repository.observeMatch(matchId).collect { match ->
                    _uiState.update {
                        it.copy(
                            match = match,
                            isLoading = false,
                            errorMessage = if (match == null) "Match not found" else null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
