package com.example.igccxi.screens.create

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
    val tossWinner: String = "",
    val tossDecision: String = "bat", // "bat" or "bowl"
    val strikerName: String = "Striker 1",
    val nonStrikerName: String = "Non-Striker 2",
    val bowlerName: String = "Bowler 1",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
