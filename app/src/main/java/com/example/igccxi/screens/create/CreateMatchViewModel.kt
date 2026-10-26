package com.example.igccxi.screens.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.ScoringEngine
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.usecase.CreateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateMatchViewModel @JvmOverloads constructor(
    application: Application,
    private val createMatchUseCase: CreateMatchUseCase = RepositoryProvider.provideCreateMatchUseCase(application)
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

    fun swapTeams() = _uiState.update {
        val newTeamA = it.teamB
        val newTeamB = it.teamA
        val newTossWinner = if (it.tossWinner == it.teamA) newTeamA else newTeamB
        it.copy(
            teamA = newTeamA,
            teamB = newTeamB,
            tossWinner = newTossWinner
        )
    }

    fun autofillSamplePlayers() = _uiState.update {
        it.copy(
            strikerName = if (it.strikerName.isEmpty()) "Batter 1" else it.strikerName,
            nonStrikerName = if (it.nonStrikerName.isEmpty()) "Batter 2" else it.nonStrikerName,
            bowlerName = if (it.bowlerName.isEmpty()) "Bowler 1" else it.bowlerName
        )
    }

    fun onPresetOversSelected(overs: Int) = _uiState.update {
        it.copy(
            totalOvers = overs,
            customOversText = overs.toString(),
            isCustomSelected = false,
            errorMessage = null
        )
    }

    fun onCustomOversSelected() = _uiState.update {
        it.copy(
            isCustomSelected = true,
            customOversText = it.totalOvers.toString()
        )
    }

    fun onCustomOversTextChange(text: String) {
        val filtered = text.filter { it.isDigit() }.take(3)
        val parsed = filtered.toIntOrNull()
        _uiState.update {
            it.copy(
                customOversText = filtered,
                totalOvers = parsed?.coerceIn(1, 100) ?: it.totalOvers,
                isCustomSelected = true,
                errorMessage = null
            )
        }
    }

    fun incrementOvers() = _uiState.update {
        val next = (it.totalOvers + 1).coerceAtMost(100)
        it.copy(
            totalOvers = next,
            customOversText = next.toString(),
            isCustomSelected = true
        )
    }

    fun decrementOvers() = _uiState.update {
        val prev = (it.totalOvers - 1).coerceAtLeast(1)
        it.copy(
            totalOvers = prev,
            customOversText = prev.toString(),
            isCustomSelected = true
        )
    }

    fun onQuickAddOvers(amount: Int) = _uiState.update {
        val next = (it.totalOvers + amount).coerceIn(1, 100)
        it.copy(
            totalOvers = next,
            customOversText = next.toString(),
            isCustomSelected = true
        )
    }

    fun onTossWinnerChange(winner: String) = _uiState.update { it.copy(tossWinner = winner) }
    fun onTossDecisionChange(decision: String) = _uiState.update { it.copy(tossDecision = decision) }
    fun onStrikerNameChange(name: String) = _uiState.update { it.copy(strikerName = name) }
    fun onNonStrikerNameChange(name: String) = _uiState.update { it.copy(nonStrikerName = name) }
    fun onBowlerNameChange(name: String) = _uiState.update { it.copy(bowlerName = name) }

    fun createMatch(onMatchCreated: (String) -> Unit) {
        val state = _uiState.value
        val teamA = state.teamA.trim()
        val teamB = state.teamB.trim()
        if (teamA.isEmpty() || teamB.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter both Team A and Team B names") }
            return
        }

        if (teamA.equals(teamB, ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Team A and Team B cannot have the same name") }
            return
        }

        val striker = state.strikerName.trim().ifEmpty { "Batter 1" }
        val nonStriker = state.nonStrikerName.trim().ifEmpty { "Batter 2" }
        val bowler = state.bowlerName.trim().ifEmpty { "Bowler 1" }

        if (striker.equals(nonStriker, ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Opening striker and non-striker cannot have the same player name") }
            return
        }

        val effectiveOvers = if (state.isCustomSelected) {
            state.customOversText.toIntOrNull() ?: state.totalOvers
        } else {
            state.totalOvers
        }

        if (effectiveOvers < 1 || effectiveOvers > 100) {
            _uiState.update { it.copy(errorMessage = "Please enter valid match overs between 1 and 100") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val initialMatch = Match(
                    teamA = teamA,
                    teamB = teamB,
                    date = state.date.trim(),
                    totalOvers = effectiveOvers,
                    tossWinner = if (state.tossWinner.isNotEmpty()) state.tossWinner else teamA,
                    tossDecision = state.tossDecision
                )

                val startedMatch = ScoringEngine.startMatch(
                    match = initialMatch,
                    strikerName = striker,
                    nonStrikerName = nonStriker,
                    bowlerName = bowler
                )

                val matchId = createMatchUseCase(startedMatch)
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
