package com.example.igccxi.screens.create

import com.example.igccxi.domain.model.PlayerProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateMatchUiState(
    val teamA: String = "",
    val teamB: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val totalOvers: Int = 20,
    val customOversText: String = "20",
    val isCustomSelected: Boolean = false,
    val playersPerTeam: Int = 11,
    val customPlayersText: String = "11",
    val isCustomPlayersSelected: Boolean = false,
    val hasCommonPlayer: Boolean = false,
    val commonPlayerName: String = "",
    val isTeamAWonToss: Boolean = true,
    val tossDecision: String = "bat", // "bat" or "bowl"
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val allProfiles: List<PlayerProfile> = emptyList(),
    val editingProfileForPlayer: String? = null,
    val showDirectoryDialog: Boolean = false
)
