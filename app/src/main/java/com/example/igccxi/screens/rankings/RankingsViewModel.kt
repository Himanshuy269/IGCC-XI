package com.example.igccxi.screens.rankings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.model.PlayerBattingRanking
import com.example.igccxi.domain.model.PlayerBowlingRanking
import com.example.igccxi.domain.usecase.GetPlayerRankingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

/**
 * ====================================================================================
 * PRESENTATION LAYER: REACTIVE RANKINGS VIEWMODEL (MVVM / Unidirectional Data Flow)
 * ====================================================================================
 *
 * [RankingsViewModel] produces the official batsman and bowler leaderboards.
 *
 * JUNIOR DEVELOPER NOTE ON DECLARATIVE STATE FLOW:
 * - Traditional (imperative) code imperatively updates fields and copies state everywhere.
 * - In Modern Android Reactive Architecture, UI state is a pure function of inputs:
 *     uiState = combine(rankingsStream, searchQuery, selectedTab, sortOptions)
 * - Whenever the user types a search query or toggles sorting, the combined pipeline
 *   automatically re-filters and emits a fresh [RankingsUiState].
 * - [.flowOn(Dispatchers.Default)]: Sorts and filters on background thread pool.
 * - [.stateIn(SharingStarted.WhileSubscribed(5000))]: Prevents memory leaks by automatically
 *   stopping background work when the user leaves the screen.
 */
class RankingsViewModel @JvmOverloads constructor(
    application: Application,
    private val getPlayerRankingsUseCase: GetPlayerRankingsUseCase = RepositoryProvider.provideGetPlayerRankingsUseCase(application)
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(RankingTab.BATSMEN)
    private val _sortByBatsmen = MutableStateFlow(BatsmenSort.RUNS)
    private val _sortByBowlers = MutableStateFlow(BowlersSort.WICKETS)

    val uiState: StateFlow<RankingsUiState> = combine(
        getPlayerRankingsUseCase(),
        _searchQuery,
        _selectedTab,
        _sortByBatsmen,
        _sortByBowlers
    ) { data, query, tab, sortBatsmen, sortBowlers ->
        val filteredBatsmen = filterAndSortBatsmen(data.batsmen, query, sortBatsmen)
        val filteredBowlers = filterAndSortBowlers(data.bowlers, query, sortBowlers)
        RankingsUiState(
            isLoading = false,
            selectedTab = tab,
            searchQuery = query,
            sortByBatsmen = sortBatsmen,
            sortByBowlers = sortBowlers,
            allBatsmen = data.batsmen,
            allBowlers = data.bowlers,
            filteredBatsmen = filteredBatsmen,
            filteredBowlers = filteredBowlers,
            totalMatchesAnalyzed = data.totalMatchesAnalyzed
        )
    }
        .flowOn(Dispatchers.Default) // Offload filtering and sorting away from Main thread
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RankingsUiState(isLoading = true)
        )

    fun onTabSelected(tab: RankingTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onBatsmenSortChanged(sort: BatsmenSort) {
        _sortByBatsmen.value = sort
    }

    fun onBowlersSortChanged(sort: BowlersSort) {
        _sortByBowlers.value = sort
    }

    private fun filterAndSortBatsmen(
        list: List<PlayerBattingRanking>,
        query: String,
        sort: BatsmenSort
    ): List<PlayerBattingRanking> {
        val queryLower = query.trim().lowercase(Locale.ROOT)
        val filtered = if (queryLower.isEmpty()) {
            list
        } else {
            list.filter { it.name.lowercase(Locale.ROOT).contains(queryLower) }
        }

        val sorted = when (sort) {
            BatsmenSort.RUNS -> filtered.sortedByDescending { it.totalRuns }
            BatsmenSort.STRIKE_RATE -> filtered.sortedByDescending { it.strikeRate }
            BatsmenSort.HIGHEST_SCORE -> filtered.sortedByDescending { it.highestScore }
            BatsmenSort.SIXES -> filtered.sortedByDescending { it.sixes }
        }

        // Reassign ranks based on current view
        return sorted.mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    private fun filterAndSortBowlers(
        list: List<PlayerBowlingRanking>,
        query: String,
        sort: BowlersSort
    ): List<PlayerBowlingRanking> {
        val queryLower = query.trim().lowercase(Locale.ROOT)
        val filtered = if (queryLower.isEmpty()) {
            list
        } else {
            list.filter { it.name.lowercase(Locale.ROOT).contains(queryLower) }
        }

        val sorted = when (sort) {
            BowlersSort.WICKETS -> filtered.sortedByDescending { it.wickets }
            BowlersSort.ECONOMY -> filtered.sortedBy { if (it.totalBalls > 0) it.economyRate else 999.0 }
            BowlersSort.MAIDENS -> filtered.sortedByDescending { it.maidens }
        }

        // Reassign ranks based on current view
        return sorted.mapIndexed { index, item -> item.copy(rank = index + 1) }
    }
}
