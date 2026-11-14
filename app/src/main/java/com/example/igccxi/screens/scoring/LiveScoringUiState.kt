package com.example.igccxi.screens.scoring

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.PlayerProfile

data class LiveScoringUiState(
    val match: Match? = null,
    val canUndo: Boolean = false,
    val showWicketDialog: Boolean = false,
    val showBowlerDialog: Boolean = false,
    val showExtrasDialog: Boolean = false,
    val isOverCompleteBowlerChange: Boolean = false,
    val completedOverNumber: Int = 0,
    val showSecondInningsDialog: Boolean = false,
    val isScorer: Boolean = true,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allProfiles: List<PlayerProfile> = emptyList(),
    val editingProfileForPlayer: String? = null,
    val showProfilesDirectory: Boolean = false
)
