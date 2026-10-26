package com.example.igccxi.screens.home

import com.example.igccxi.domain.model.Match

enum class HomeFilter {
    ALL, LIVE, COMPLETED
}

data class HomeUiState(
    val liveMatches: List<Match> = emptyList(),
    val recentMatches: List<Match> = emptyList(),
    val currentUserId: String = "",
    val activeUsersCount: Int = 1,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
