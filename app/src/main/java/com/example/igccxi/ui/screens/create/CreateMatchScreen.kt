package com.example.igccxi.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onNavigateBack: () -> Unit,
    onMatchCreated: (String) -> Unit,
    viewModel: CreateMatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Match", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Match Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Team A
            OutlinedTextField(
                value = uiState.teamA,
                onValueChange = { viewModel.onTeamAChange(it) },
                label = { Text("Team A Name") },
                placeholder = { Text("e.g. Royal Challengers") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Team B
            OutlinedTextField(
                value = uiState.teamB,
                onValueChange = { viewModel.onTeamBChange(it) },
                label = { Text("Team B Name") },
                placeholder = { Text("e.g. Super Kings") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Date
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { viewModel.onDateChange(it) },
                label = { Text("Match Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Overs Selection
            Text(
                text = "Total Overs: ${uiState.totalOvers}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 20).forEach { overs ->
                    FilterChip(
                        selected = uiState.totalOvers == overs,
                        onClick = { viewModel.onOversChange(overs) },
                        label = { Text("$overs Ov") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Toss Section
            Text(
                text = "Toss Winner & Decision",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Opted to:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.tossDecision == "bat",
                        onClick = { viewModel.onTossDecisionChange("bat") }
                    )
                    Text("Bat First", modifier = Modifier.padding(end = 16.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.tossDecision == "bowl",
                        onClick = { viewModel.onTossDecisionChange("bowl") }
                    )
                    Text("Bowl First")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Opening Players Section
            Text(
                text = "Opening Players",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.strikerName,
                onValueChange = { viewModel.onStrikerNameChange(it) },
                label = { Text("Striker Name (Opening Batter 1)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.nonStrikerName,
                onValueChange = { viewModel.onNonStrikerNameChange(it) },
                label = { Text("Non-Striker Name (Opening Batter 2)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.bowlerName,
                onValueChange = { viewModel.onBowlerNameChange(it) },
                label = { Text("Opening Bowler Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.createMatch(onMatchCreated) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(
                        text = "Start Scoring Match",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
