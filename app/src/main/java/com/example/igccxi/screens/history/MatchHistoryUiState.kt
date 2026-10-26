package com.example.igccxi.screens.history

import com.example.igccxi.domain.model.Match
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MatchHistoryUiState(
    val matches: List<Match> = emptyList(),
    val totalMatchesCount: Int = 0,
    val selectedDate: String = "", // Empty indicates "All Dates"
    val searchQuery: String = "",
    val availableDates: List<String> = emptyList(),
    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val yesterdayDate: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
