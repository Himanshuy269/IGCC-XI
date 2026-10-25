package com.example.igccxi.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.data.model.Match
import com.example.igccxi.ui.components.ScoreCardSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateMatch: () -> Unit,
    onNavigateToScoring: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "IGCC XI Live Cricket",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Match History by Date"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateMatch,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Match") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Live Matches
                item {
                    Text(
                        text = "Live Matches (${uiState.liveMatches.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (uiState.liveMatches.isEmpty()) {
                    item {
                        CardEmptyNotice(message = "No live matches in progress right now. Tap 'Create Match' to start scoring!")
                    }
                } else {
                    items(uiState.liveMatches, key = { it.id }) { match ->
                        ScoreCardSummary(
                            match = match,
                            onClick = {
                                if (match.scorerId == uiState.currentUserId && match.isScorable) {
                                    onNavigateToScoring(match.id)
                                } else {
                                    onNavigateToDetails(match.id)
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Section: Recent Completed Matches
                item {
                    Text(
                        text = "Recent Matches",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (uiState.recentMatches.isEmpty()) {
                    item {
                        CardEmptyNotice(message = "No completed matches yet.")
                    }
                } else {
                    items(uiState.recentMatches.take(5), key = { it.id }) { match ->
                        ScoreCardSummary(
                            match = match,
                            onClick = { onNavigateToDetails(match.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(60.dp)) } // padding for FAB
            }
        }
    }
}

@Composable
fun CardEmptyNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
