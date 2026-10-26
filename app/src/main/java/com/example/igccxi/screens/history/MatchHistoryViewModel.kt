package com.example.igccxi.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.igccxi.data.repository.RepositoryProvider
import com.example.igccxi.domain.usecase.GetMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MatchHistoryViewModel @JvmOverloads constructor(
    application: Application,
    private val getMatchesUseCase: GetMatchesUseCase = RepositoryProvider.provideGetMatchesUseCase(application)
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")

    private val todayFormatted: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val yesterdayFormatted: String = run {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    val uiState: StateFlow<MatchHistoryUiState> = combine(
        getMatchesUseCase(),
        _selectedDate,
        _searchQuery
    ) { allMatches, date, query ->
        val dates = allMatches.map { it.date }.filter { it.isNotEmpty() }.distinct().sortedDescending()

        val filtered = allMatches.filter { match ->
            val matchesDate = date.isEmpty() || match.date == date
            val matchesSearch = query.trim().isEmpty() ||
                    match.teamA.contains(query.trim(), ignoreCase = true) ||
                    match.teamB.contains(query.trim(), ignoreCase = true) ||
                    match.id.contains(query.trim(), ignoreCase = true)
            matchesDate && matchesSearch
        }

        MatchHistoryUiState(
            matches = filtered,
            totalMatchesCount = allMatches.size,
            selectedDate = date,
            searchQuery = query,
            availableDates = dates,
            todayDate = todayFormatted,
            yesterdayDate = yesterdayFormatted,
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

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun onClearDateFilter() {
        _selectedDate.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
