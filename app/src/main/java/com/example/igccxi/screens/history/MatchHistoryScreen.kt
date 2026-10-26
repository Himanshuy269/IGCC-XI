package com.example.igccxi.screens.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.ui.components.ScoreCardSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: MatchHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateScrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = utcMillis
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val formattedDate = sdf.format(calendar.time)
                            viewModel.onDateSelected(formattedDate)
                        }
                    }
                ) {
                    Text("Select Date", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Scorecard Archive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (uiState.selectedDate.isNotEmpty()) "Date: ${uiState.selectedDate}" else "Search by Date & Calendar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Open Calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Team Name / ID Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search by team name or match ID...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Calendar Selector Section Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search by Date",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Button(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selected Date Active Banner
                    if (uiState.selectedDate.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Filter: ${uiState.selectedDate} (${uiState.matches.size} found)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.onClearDateFilter() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Clear",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Quick Date Chips (All Dates, Today, Yesterday, Recorded Dates)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(dateScrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedDate.isEmpty(),
                            onClick = { viewModel.onClearDateFilter() },
                            label = { Text("All Dates (${uiState.totalMatchesCount})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors()
                        )

                        // Today Chip
                        FilterChip(
                            selected = uiState.selectedDate == uiState.todayDate,
                            onClick = { viewModel.onDateSelected(uiState.todayDate) },
                            label = { Text("Today", fontSize = 11.sp) }
                        )

                        // Yesterday Chip
                        if (uiState.yesterdayDate.isNotEmpty()) {
                            FilterChip(
                                selected = uiState.selectedDate == uiState.yesterdayDate,
                                onClick = { viewModel.onDateSelected(uiState.yesterdayDate) },
                                label = { Text("Yesterday", fontSize = 11.sp) }
                            )
                        }

                        // Specific recorded match dates
                        uiState.availableDates
                            .filter { it != uiState.todayDate && it != uiState.yesterdayDate }
                            .forEach { date ->
                                FilterChip(
                                    selected = uiState.selectedDate == date,
                                    onClick = { viewModel.onDateSelected(date) },
                                    label = { Text(date, fontSize = 11.sp) }
                                )
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Matches List or Empty State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (uiState.selectedDate.isNotEmpty())
                                    "No Scorecards on ${uiState.selectedDate}"
                                else
                                    "No Matches Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (uiState.selectedDate.isNotEmpty())
                                    "No cricket matches were recorded for this date. You can select another date from the calendar or view all matches."
                                else
                                    "No matches match your search query. Try another term or clear filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Date")
                                }

                                Button(
                                    onClick = {
                                        viewModel.onClearDateFilter()
                                        viewModel.onSearchQueryChanged("")
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Show All")
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.matches, key = { it.id }) { match ->
                        ScoreCardSummary(
                            match = match,
                            onClick = { onNavigateToDetails(match.id) }
                        )
                    }
                }
            }
        }
    }
}
