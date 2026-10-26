package com.example.igccxi.screens.details

import com.example.igccxi.domain.model.Match

data class MatchDetailsUiState(
    val match: Match? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
