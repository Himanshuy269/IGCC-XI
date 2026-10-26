package com.example.igccxi.screens.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.usecase.GetCurrentUserIdUseCase
import com.example.igccxi.domain.usecase.GetMatchByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchDetailsViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchByIdUseCase: GetMatchByIdUseCase = RepositoryProvider.provideGetMatchByIdUseCase(application),
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = RepositoryProvider.provideGetCurrentUserIdUseCase(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState: StateFlow<MatchDetailsUiState> = _uiState.asStateFlow()

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            try {
                val uid = getCurrentUserIdUseCase()
                _uiState.update { it.copy(currentUserId = uid) }

                getMatchByIdUseCase(matchId).collect { match ->
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
