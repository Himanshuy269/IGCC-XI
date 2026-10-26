package com.example.igccxi.screens.rankings

import com.example.igccxi.domain.model.PlayerBattingRanking
import com.example.igccxi.domain.model.PlayerBowlingRanking

enum class RankingTab {
    BATSMEN,
    BOWLERS
}

enum class BatsmenSort {
    RUNS,
    STRIKE_RATE,
    HIGHEST_SCORE,
    SIXES
}

enum class BowlersSort {
    WICKETS,
    ECONOMY,
    MAIDENS
}

data class RankingsUiState(
    val isLoading: Boolean = true,
    val selectedTab: RankingTab = RankingTab.BATSMEN,
    val searchQuery: String = "",
    val allBatsmen: List<PlayerBattingRanking> = emptyList(),
    val allBowlers: List<PlayerBowlingRanking> = emptyList(),
    val filteredBatsmen: List<PlayerBattingRanking> = emptyList(),
    val filteredBowlers: List<PlayerBowlingRanking> = emptyList(),
    val sortByBatsmen: BatsmenSort = BatsmenSort.RUNS,
    val sortByBowlers: BowlersSort = BowlersSort.WICKETS,
    val totalMatchesAnalyzed: Int = 0
)
