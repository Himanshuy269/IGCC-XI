package com.example.igccxi.screens.rankings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.model.PlayerBattingRanking
import com.example.igccxi.domain.model.PlayerBowlingRanking
import com.example.igccxi.domain.usecase.GetPlayerRankingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class RankingsViewModel @JvmOverloads constructor(
    application: Application,
    private val getPlayerRankingsUseCase: GetPlayerRankingsUseCase = RepositoryProvider.provideGetPlayerRankingsUseCase(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    private fun loadRankings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPlayerRankingsUseCase().collect { data ->
                _uiState.update { current ->
                    val filteredBatsmen = filterAndSortBatsmen(data.batsmen, current.searchQuery, current.sortByBatsmen)
                    val filteredBowlers = filterAndSortBowlers(data.bowlers, current.searchQuery, current.sortByBowlers)
                    current.copy(
                        isLoading = false,
                        allBatsmen = data.batsmen,
                        allBowlers = data.bowlers,
                        filteredBatsmen = filteredBatsmen,
                        filteredBowlers = filteredBowlers,
                        totalMatchesAnalyzed = data.totalMatchesAnalyzed
                    )
                }
            }
        }
    }

    fun onTabSelected(tab: RankingTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filteredBatsmen = filterAndSortBatsmen(current.allBatsmen, query, current.sortByBatsmen)
            val filteredBowlers = filterAndSortBowlers(current.allBowlers, query, current.sortByBowlers)
            current.copy(
                searchQuery = query,
                filteredBatsmen = filteredBatsmen,
                filteredBowlers = filteredBowlers
            )
        }
    }

    fun onBatsmenSortChanged(sort: BatsmenSort) {
        _uiState.update { current ->
            val filteredBatsmen = filterAndSortBatsmen(current.allBatsmen, current.searchQuery, sort)
            current.copy(
                sortByBatsmen = sort,
                filteredBatsmen = filteredBatsmen
            )
        }
    }

    fun onBowlersSortChanged(sort: BowlersSort) {
        _uiState.update { current ->
            val filteredBowlers = filterAndSortBowlers(current.allBowlers, current.searchQuery, sort)
            current.copy(
                sortByBowlers = sort,
                filteredBowlers = filteredBowlers
            )
        }
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
