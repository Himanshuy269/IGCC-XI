package com.example.igccxi.screens.scoring

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.LocalPlayerProfileRepository
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.ScoringEngine
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.domain.usecase.GetCurrentUserIdUseCase
import com.example.igccxi.domain.usecase.GetMatchByIdUseCase
import com.example.igccxi.domain.usecase.UpdateMatchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ====================================================================================
 * PRESENTATION LAYER: LIVE SCORING VIEWMODEL
 * ====================================================================================
 *
 * [LiveScoringViewModel] drives the real-time scoring engine screen.
 * It manages ball-by-ball updates, undo stack, extras, wicket popups, and bowler rotation.
 */
class LiveScoringViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchByIdUseCase: GetMatchByIdUseCase = RepositoryProvider.provideGetMatchByIdUseCase(application),
    private val updateMatchUseCase: UpdateMatchUseCase = RepositoryProvider.provideUpdateMatchUseCase(application),
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = RepositoryProvider.provideGetCurrentUserIdUseCase(application),
    private val playerProfileRepository: LocalPlayerProfileRepository = RepositoryProvider.getPlayerProfileRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LiveScoringUiState())
    val uiState: StateFlow<LiveScoringUiState> = _uiState.asStateFlow()

    private val historyStack = mutableListOf<Match>()
    private var currentMatchId: String = ""
    private var observeMatchJob: Job? = null

    init {
        viewModelScope.launch {
            playerProfileRepository.observeProfiles().collect { profiles ->
                _uiState.update { it.copy(allProfiles = profiles) }
            }
        }
    }

    fun loadMatch(matchId: String) {
        currentMatchId = matchId
        observeMatchJob?.cancel() // Cancel prior match observation to prevent leak
        observeMatchJob = viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserIdUseCase()
                getMatchByIdUseCase(matchId).collect { match ->
                    if (match != null) {
                        _uiState.update {
                            it.copy(
                                match = match,
                                isScorer = match.scorerId.isEmpty() || match.scorerId == currentUserId,
                                canUndo = historyStack.isNotEmpty(),
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Match not found") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun pushHistory(match: Match) {
        historyStack.add(match)
        if (historyStack.size > 30) {
            historyStack.removeAt(0)
        }
        _uiState.update { it.copy(canUndo = true) }
    }

    fun recordRuns(runs: Int) {
        val current = _uiState.value.match ?: return
        val prevOvers = current.currentInnings.oversBowled
        pushHistory(current)
        val updated = ScoringEngine.recordRuns(current, runs)
        updateMatchInRepository(updated)

        // If this ball completed the over, trigger Bowler change popup
        if (updated.isScorable && updated.currentInnings.oversBowled > prevOvers) {
            _uiState.update {
                it.copy(
                    showBowlerDialog = true,
                    isOverCompleteBowlerChange = true,
                    completedOverNumber = updated.currentInnings.oversBowled
                )
            }
        }
    }

    fun recordWide(extraRuns: Int = 1) {
        val current = _uiState.value.match ?: return
        pushHistory(current)
        val updated = ScoringEngine.recordWide(current, extraRuns)
        updateMatchInRepository(updated)
    }

    fun recordNoBall(runsOffBat: Int = 0) {
        val current = _uiState.value.match ?: return
        pushHistory(current)
        val updated = ScoringEngine.recordNoBall(current, runsOffBat)
        updateMatchInRepository(updated)
    }

    fun showWicketDialog() = _uiState.update { it.copy(showWicketDialog = true) }
    fun dismissWicketDialog() = _uiState.update { it.copy(showWicketDialog = false) }

    fun showExtrasDialog() = _uiState.update { it.copy(showExtrasDialog = true) }
    fun dismissExtrasDialog() = _uiState.update { it.copy(showExtrasDialog = false) }

    fun recordBye(runs: Int = 1) {
        val current = _uiState.value.match ?: return
        val prevOvers = current.currentInnings.oversBowled
        pushHistory(current)
        val updated = ScoringEngine.recordBye(current, runs)
        dismissExtrasDialog()
        updateMatchInRepository(updated)

        if (updated.isScorable && updated.currentInnings.oversBowled > prevOvers) {
            _uiState.update {
                it.copy(
                    showBowlerDialog = true,
                    isOverCompleteBowlerChange = true,
                    completedOverNumber = updated.currentInnings.oversBowled
                )
            }
        }
    }

    fun recordLegBye(runs: Int = 1) {
        val current = _uiState.value.match ?: return
        val prevOvers = current.currentInnings.oversBowled
        pushHistory(current)
        val updated = ScoringEngine.recordLegBye(current, runs)
        dismissExtrasDialog()
        updateMatchInRepository(updated)

        if (updated.isScorable && updated.currentInnings.oversBowled > prevOvers) {
            _uiState.update {
                it.copy(
                    showBowlerDialog = true,
                    isOverCompleteBowlerChange = true,
                    completedOverNumber = updated.currentInnings.oversBowled
                )
            }
        }
    }

    fun recordWicket(dismissal: String, newBatterName: String?, isStrikerOut: Boolean = true) {
        val current = _uiState.value.match ?: return
        val prevOvers = current.currentInnings.oversBowled
        pushHistory(current)

        val cleanNewBatter = newBatterName?.trim()?.ifEmpty { null }
        if (cleanNewBatter != null) {
            viewModelScope.launch {
                playerProfileRepository.registerPlayerName(cleanNewBatter)
            }
        }

        val updated = ScoringEngine.recordWicket(
            match = current,
            dismissalDescription = dismissal,
            newBatterName = cleanNewBatter,
            isStrikerOut = isStrikerOut
        )
        dismissWicketDialog()
        updateMatchInRepository(updated)

        // If wicket ball completed the over, trigger Bowler change popup
        if (updated.isScorable && updated.currentInnings.oversBowled > prevOvers) {
            _uiState.update {
                it.copy(
                    showBowlerDialog = true,
                    isOverCompleteBowlerChange = true,
                    completedOverNumber = updated.currentInnings.oversBowled
                )
            }
        }
    }

    fun undoLastBall() {
        if (historyStack.isEmpty()) return
        val previousState = historyStack.removeAt(historyStack.lastIndex)
        _uiState.update {
            it.copy(
                canUndo = historyStack.isNotEmpty(),
                showBowlerDialog = false,
                isOverCompleteBowlerChange = false
            )
        }
        updateMatchInRepository(previousState)
    }

    fun swapStrike() {
        val current = _uiState.value.match ?: return
        val updated = ScoringEngine.swapStrike(current)
        updateMatchInRepository(updated)
    }

    fun showBowlerDialog() = _uiState.update {
        it.copy(
            showBowlerDialog = true,
            isOverCompleteBowlerChange = false
        )
    }

    fun dismissBowlerDialog() = _uiState.update {
        it.copy(
            showBowlerDialog = false,
            isOverCompleteBowlerChange = false
        )
    }

    fun selectBowler(bowlerName: String) {
        val clean = bowlerName.trim()
        val current = _uiState.value.match ?: return
        if (clean.isNotEmpty()) {
            viewModelScope.launch {
                playerProfileRepository.registerPlayerName(clean)
            }
        }
        val updated = ScoringEngine.selectBowler(current, clean)
        dismissBowlerDialog()
        updateMatchInRepository(updated)
    }

    fun endInnings() {
        val current = _uiState.value.match ?: return
        pushHistory(current)
        val updated = ScoringEngine.endInnings(current)
        updateMatchInRepository(updated)

        if (updated.status == MatchStatus.INNINGS_BREAK) {
            _uiState.update { it.copy(showSecondInningsDialog = true) }
        }
    }

    fun dismissSecondInningsDialog() = _uiState.update { it.copy(showSecondInningsDialog = false) }

    fun startSecondInnings(striker: String, nonStriker: String, bowler: String) {
        val current = _uiState.value.match ?: return
        val cleanStriker = striker.trim().ifEmpty { "Chasing Striker" }
        val cleanNonStriker = nonStriker.trim().ifEmpty { "Chasing Non-Striker" }
        val cleanBowler = bowler.trim().ifEmpty { "Opening Bowler" }

        viewModelScope.launch {
            playerProfileRepository.registerPlayerName(cleanStriker)
            playerProfileRepository.registerPlayerName(cleanNonStriker)
            playerProfileRepository.registerPlayerName(cleanBowler)
        }

        val updated = ScoringEngine.startSecondInnings(
            match = current,
            strikerName = cleanStriker,
            nonStrikerName = cleanNonStriker,
            bowlerName = cleanBowler
        )
        dismissSecondInningsDialog()
        updateMatchInRepository(updated)
    }

    fun updateMatchOvers(newOvers: Int) {
        val current = _uiState.value.match ?: return
        if (newOvers < 1 || newOvers > 100) return
        pushHistory(current)
        var updated = current.copy(totalOvers = newOvers)
        if (updated.isScorable && updated.currentInnings.oversBowled >= newOvers) {
            updated = ScoringEngine.endInnings(updated)
            if (updated.status == MatchStatus.INNINGS_BREAK) {
                _uiState.update { it.copy(showSecondInningsDialog = true) }
            }
        }
        updateMatchInRepository(updated)
    }

    fun updateBatterName(batterId: String, newName: String) {
        val current = _uiState.value.match ?: return
        val clean = newName.trim()
        if (clean.isBlank()) return
        pushHistory(current)
        viewModelScope.launch {
            playerProfileRepository.registerPlayerName(clean)
        }
        val updated = ScoringEngine.updateBatterName(current, batterId, clean)
        updateMatchInRepository(updated)
    }

    fun updateBowlerName(bowlerId: String, newName: String) {
        val current = _uiState.value.match ?: return
        val clean = newName.trim()
        if (clean.isBlank()) return
        pushHistory(current)
        viewModelScope.launch {
            playerProfileRepository.registerPlayerName(clean)
        }
        val updated = ScoringEngine.updateBowlerName(current, bowlerId, clean)
        updateMatchInRepository(updated)
    }

    // --- Profile Management in Live Scoring ---
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

    fun setShowProfilesDirectory(show: Boolean) {
        _uiState.update { it.copy(showProfilesDirectory = show) }
    }

    fun onAddNewPlayerToDirectory(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            playerProfileRepository.registerPlayerName(clean)
        }
    }

    private fun updateMatchInRepository(match: Match) {
        viewModelScope.launch {
            try {
                updateMatchUseCase(match)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error updating score: ${e.message}") }
            }
        }
    }
}
