package com.example.igccxi.screens.create

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.LocalPlayerProfileRepository
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
    private val createMatchUseCase: CreateMatchUseCase = RepositoryProvider.provideCreateMatchUseCase(application),
    private val playerProfileRepository: LocalPlayerProfileRepository = RepositoryProvider.getPlayerProfileRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateMatchUiState())
    val uiState: StateFlow<CreateMatchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playerProfileRepository.observeProfiles().collect { profiles ->
                _uiState.update { it.copy(allProfiles = profiles) }
            }
        }
    }

    fun onTeamAChange(name: String) = _uiState.update { it.copy(teamA = name) }
    fun onTeamBChange(name: String) = _uiState.update { it.copy(teamB = name) }
    fun onDateChange(date: String) = _uiState.update { it.copy(date = date) }

    fun swapTeams() = _uiState.update {
        it.copy(
            teamA = it.teamB,
            teamB = it.teamA,
            isTeamAWonToss = !it.isTeamAWonToss
        )
    }

    fun autofillSamplePlayers() {
        // Disabled: users must enter real player names to proceed
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

    fun onPresetPlayersSelected(players: Int) = _uiState.update {
        it.copy(
            playersPerTeam = players,
            customPlayersText = players.toString(),
            isCustomPlayersSelected = false,
            errorMessage = null
        )
    }

    fun onCustomPlayersSelected() = _uiState.update {
        it.copy(
            isCustomPlayersSelected = true,
            customPlayersText = it.playersPerTeam.toString()
        )
    }

    fun onCustomPlayersTextChange(text: String) {
        val filtered = text.filter { it.isDigit() }.take(2)
        val parsed = filtered.toIntOrNull()
        _uiState.update {
            it.copy(
                customPlayersText = filtered,
                playersPerTeam = parsed?.coerceIn(2, 30) ?: it.playersPerTeam,
                isCustomPlayersSelected = true,
                errorMessage = null
            )
        }
    }

    fun incrementPlayers() = _uiState.update {
        val next = (it.playersPerTeam + 1).coerceAtMost(30)
        it.copy(
            playersPerTeam = next,
            customPlayersText = next.toString(),
            isCustomPlayersSelected = true
        )
    }

    fun decrementPlayers() = _uiState.update {
        val prev = (it.playersPerTeam - 1).coerceAtLeast(2)
        it.copy(
            playersPerTeam = prev,
            customPlayersText = prev.toString(),
            isCustomPlayersSelected = true
        )
    }

    fun onHasCommonPlayerChange(enabled: Boolean) = _uiState.update {
        it.copy(
            hasCommonPlayer = enabled,
            commonPlayerName = if (enabled && it.commonPlayerName.isEmpty()) "Common Player" else it.commonPlayerName,
            errorMessage = null
        )
    }
    fun onCommonPlayerNameChange(name: String) = _uiState.update {
        it.copy(commonPlayerName = name, errorMessage = null)
    }

    fun setTossWinnerIsTeamA(isTeamA: Boolean) = _uiState.update { it.copy(isTeamAWonToss = isTeamA) }
    fun onTossWinnerChange(isTeamA: Boolean) = _uiState.update { it.copy(isTeamAWonToss = isTeamA) }
    fun onTossDecisionChange(decision: String) = _uiState.update { it.copy(tossDecision = decision) }
    fun onStrikerNameChange(name: String) = _uiState.update { it.copy(strikerName = name) }
    fun onNonStrikerNameChange(name: String) = _uiState.update { it.copy(nonStrikerName = name) }
    fun onBowlerNameChange(name: String) = _uiState.update { it.copy(bowlerName = name) }

    // --- Player Profile Actions ---
    fun onRequestAddProfile(playerName: String) {
        val clean = playerName.trim()
        if (clean.isNotEmpty()) {
            viewModelScope.launch {
                playerProfileRepository.registerPlayerName(clean)
                _uiState.update { it.copy(editingProfileForPlayer = clean) }
            }
        }
    }

    fun dismissAddProfileDialog() {
        _uiState.update { it.copy(editingProfileForPlayer = null) }
    }

    fun onPhotoSelected(playerName: String, uri: Uri) {
        val clean = playerName.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val savedPath = playerProfileRepository.saveImageFromUri(clean, uri)
            if (savedPath != null) {
                playerProfileRepository.updateProfilePhoto(clean, savedPath, null)
            }
            dismissAddProfileDialog()
        }
    }

    fun onAvatarPresetSelected(playerName: String, icon: String) {
        val clean = playerName.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            playerProfileRepository.updateProfilePhoto(clean, null, icon)
            dismissAddProfileDialog()
        }
    }

    fun onRemovePhoto(playerName: String) {
        val clean = playerName.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            playerProfileRepository.updateProfilePhoto(clean, null, null)
            dismissAddProfileDialog()
        }
    }

    fun setShowDirectoryDialog(show: Boolean) {
        _uiState.update { it.copy(showDirectoryDialog = show) }
    }

    fun onAddNewPlayerToDirectory(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            playerProfileRepository.registerPlayerName(clean)
        }
    }

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

        val striker = state.strikerName.trim()
        val nonStriker = state.nonStrikerName.trim()
        val bowler = state.bowlerName.trim()

        if (striker.isEmpty() || nonStriker.isEmpty() || bowler.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter Striker, Non-Striker, and Bowler names to proceed") }
            return
        }

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

        val effectivePlayers = if (state.isCustomPlayersSelected) {
            state.customPlayersText.toIntOrNull() ?: state.playersPerTeam
        } else {
            state.playersPerTeam
        }

        if (effectivePlayers < 2 || effectivePlayers > 30) {
            _uiState.update { it.copy(errorMessage = "Please enter valid number of players per team (2 to 30)") }
            return
        }

        val commonPlayer = if (state.hasCommonPlayer) state.commonPlayerName.trim() else ""
        if (state.hasCommonPlayer && commonPlayer.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter the Common Player's name") }
            return
        }

        val totalBattersPerTeam = if (state.hasCommonPlayer) effectivePlayers + 1 else effectivePlayers

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                // Automatically persist every player into the app's player profiles
                playerProfileRepository.registerPlayerName(striker)
                playerProfileRepository.registerPlayerName(nonStriker)
                playerProfileRepository.registerPlayerName(bowler)
                if (commonPlayer.isNotEmpty()) {
                    playerProfileRepository.registerPlayerName(commonPlayer)
                }

                val actualTossWinner = if (state.isTeamAWonToss) teamA else teamB
                val initialMatch = Match(
                    teamA = teamA,
                    teamB = teamB,
                    date = state.date.trim(),
                    totalOvers = effectiveOvers,
                    playersPerTeam = totalBattersPerTeam,
                    tossWinner = actualTossWinner,
                    tossDecision = state.tossDecision,
                    commonPlayerName = commonPlayer
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
