package com.example.igccxi.ui.screens.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.repository.MatchRepository
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.ScoringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateMatchUiState(
    val teamA: String = "",
    val teamB: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val totalOvers: Int = 20,
    val tossWinner: String = "",
    val tossDecision: String = "bat", // "bat" or "bowl"
    val strikerName: String = "Striker 1",
    val nonStrikerName: String = "Non-Striker 2",
    val bowlerName: String = "Bowler 1",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class CreateMatchViewModel(
    application: Application,
    private val repository: MatchRepository = RepositoryProvider.getRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateMatchUiState())
    val uiState: StateFlow<CreateMatchUiState> = _uiState.asStateFlow()

    fun onTeamAChange(name: String) = _uiState.update {
        it.copy(
            teamA = name,
            tossWinner = if (it.tossWinner.isEmpty()) name else it.tossWinner
        )
    }

    fun onTeamBChange(name: String) = _uiState.update { it.copy(teamB = name) }
    fun onDateChange(date: String) = _uiState.update { it.copy(date = date) }
    fun onOversChange(overs: Int) = _uiState.update { it.copy(totalOvers = overs) }
    fun onTossWinnerChange(winner: String) = _uiState.update { it.copy(tossWinner = winner) }
    fun onTossDecisionChange(decision: String) = _uiState.update { it.copy(tossDecision = decision) }
    fun onStrikerNameChange(name: String) = _uiState.update { it.copy(strikerName = name) }
    fun onNonStrikerNameChange(name: String) = _uiState.update { it.copy(nonStrikerName = name) }
    fun onBowlerNameChange(name: String) = _uiState.update { it.copy(bowlerName = name) }

    fun createMatch(onMatchCreated: (String) -> Unit) {
        val state = _uiState.value
        if (state.teamA.trim().isEmpty() || state.teamB.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter both Team A and Team B names") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val initialMatch = Match(
                    teamA = state.teamA.trim(),
                    teamB = state.teamB.trim(),
                    date = state.date.trim(),
                    totalOvers = state.totalOvers,
                    tossWinner = if (state.tossWinner.isNotEmpty()) state.tossWinner else state.teamA.trim(),
                    tossDecision = state.tossDecision
                )

                val startedMatch = ScoringEngine.startMatch(
                    match = initialMatch,
                    strikerName = state.strikerName.trim().ifEmpty { "Striker 1" },
                    nonStrikerName = state.nonStrikerName.trim().ifEmpty { "Non-Striker 2" },
                    bowlerName = state.bowlerName.trim().ifEmpty { "Bowler 1" }
                )

                val matchId = repository.createMatch(startedMatch)
                _uiState.update { it.copy(isSubmitting = false) }
                onMatchCreated(matchId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Error creating match: ${e.message}"
                    )
                }
            }
        }
    }
}
