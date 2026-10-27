package com.example.igccxi.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.domain.usecase.DeleteMatchUseCase
import com.example.igccxi.domain.usecase.GetCurrentUserIdUseCase
import com.example.igccxi.domain.usecase.GetMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MatchHistoryViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchesUseCase: GetMatchesUseCase = RepositoryProvider.provideGetMatchesUseCase(application),
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = RepositoryProvider.provideGetCurrentUserIdUseCase(application),
    private val deleteMatchUseCase: DeleteMatchUseCase = RepositoryProvider.provideDeleteMatchUseCase(application)
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow(ScorecardFilter.ALL)
    private val _userId = MutableStateFlow("")

    private val todayFormatted: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val yesterdayFormatted: String = run {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    init {
        viewModelScope.launch {
            try {
                _userId.value = getCurrentUserIdUseCase()
            } catch (_: Exception) {
            }
        }
    }

    val uiState: StateFlow<MatchHistoryUiState> = combine(
        getMatchesUseCase(),
        _selectedDate,
        _searchQuery,
        _statusFilter,
        _userId
    ) { allMatches, date, query, statusFilter, uid ->
        val dates = allMatches.map { it.date }.filter { it.isNotEmpty() }.distinct().sortedDescending()

        val liveCount = allMatches.count {
            it.status == MatchStatus.INNINGS_1 ||
            it.status == MatchStatus.INNINGS_BREAK ||
            it.status == MatchStatus.INNINGS_2
        }
        val completedCount = allMatches.count { it.status == MatchStatus.COMPLETED }

        val filtered = allMatches.filter { match ->
            val matchesDate = date.isEmpty() || match.date == date
            val matchesSearch = query.trim().isEmpty() ||
                    match.teamA.contains(query.trim(), ignoreCase = true) ||
                    match.teamB.contains(query.trim(), ignoreCase = true) ||
                    match.id.contains(query.trim(), ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                ScorecardFilter.ALL -> true
                ScorecardFilter.LIVE -> match.status == MatchStatus.INNINGS_1 ||
                        match.status == MatchStatus.INNINGS_BREAK ||
                        match.status == MatchStatus.INNINGS_2
                ScorecardFilter.COMPLETED -> match.status == MatchStatus.COMPLETED
            }
            matchesDate && matchesSearch && matchesStatus
        }

        MatchHistoryUiState(
            matches = filtered,
            totalMatchesCount = allMatches.size,
            liveMatchesCount = liveCount,
            completedMatchesCount = completedCount,
            selectedFilter = statusFilter,
            selectedDate = date,
            searchQuery = query,
            availableDates = dates,
            todayDate = todayFormatted,
            yesterdayDate = yesterdayFormatted,
            currentUserId = uid,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchHistoryUiState(
            todayDate = todayFormatted,
            yesterdayDate = yesterdayFormatted,
            isLoading = true
        )
    )

    fun onStatusFilterSelected(filter: ScorecardFilter) {
        _statusFilter.value = filter
    }

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun onClearDateFilter() {
        _selectedDate.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onClearAllFilters() {
        _selectedDate.value = ""
        _searchQuery.value = ""
        _statusFilter.value = ScorecardFilter.ALL
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            try {
                deleteMatchUseCase(matchId)
            } catch (_: Exception) {
            }
        }
    }
}
