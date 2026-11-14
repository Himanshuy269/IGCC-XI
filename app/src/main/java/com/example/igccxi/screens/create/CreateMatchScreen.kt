package com.example.igccxi.screens.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.components.CommonPlayerBadge
import com.example.igccxi.ui.components.PlayerAutocompleteTextField
import com.example.igccxi.ui.components.AddProfilePhotoDialog
import com.example.igccxi.ui.components.PlayerProfilesDirectoryDialog
import com.example.igccxi.ui.theme.CommonPlayerBlue
import com.example.igccxi.ui.theme.CommonPlayerBlueContainer
import com.example.igccxi.ui.theme.CommonPlayerBlueText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onNavigateBack: () -> Unit,
    onMatchCreated: (String) -> Unit,
    viewModel: CreateMatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val teamAName = uiState.teamA.ifEmpty { "Team A" }
    val teamBName = uiState.teamB.ifEmpty { "Team B" }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("New Match Setup", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // Section 1: Teams Matchup Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEAMS MATCHUP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        Surface(
                            onClick = { viewModel.swapTeams() },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Swap Teams", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Swap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.teamA,
                        onValueChange = { viewModel.onTeamAChange(it) },
                        label = { Text("Team 1 (e.g. Royal Challengers)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.teamB,
                        onValueChange = { viewModel.onTeamBChange(it) },
                        label = { Text("Team 2 (e.g. Super Kings)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // -------------------------------------------------------------
            // Section 2: Match Overs Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MATCH OVERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        val displayOvers = if (uiState.isCustomSelected) {
                            uiState.customOversText.ifEmpty { "0" }
                        } else {
                            uiState.totalOvers.toString()
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$displayOvers Overs Per Side",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips Row: 5, 8, 10, 15, 20, Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 8, 10, 15, 20).forEach { overs ->
                            FilterChip(
                                selected = !uiState.isCustomSelected && uiState.totalOvers == overs,
                                onClick = { viewModel.onPresetOversSelected(overs) },
                                label = { Text("${overs} Ov", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FilterChip(
                            selected = uiState.isCustomSelected,
                            onClick = { viewModel.onCustomOversSelected() },
                            label = { Text("Custom", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    if (uiState.isCustomSelected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Overs limit (1-100):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { viewModel.decrementOvers() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }

                                OutlinedTextField(
                                    value = uiState.customOversText,
                                    onValueChange = { viewModel.onCustomOversTextChange(it) },
                                    modifier = Modifier.width(64.dp),
                                    textStyle = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                FilledTonalIconButton(
                                    onClick = { viewModel.incrementOvers() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Section 2B: Players Per Side Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLAYERS PER SIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        val displayPlayers = if (uiState.isCustomPlayersSelected) {
                            uiState.customPlayersText.ifEmpty { "0" }
                        } else {
                            uiState.playersPerTeam.toString()
                        }
                        val maxW = maxOf(1, displayPlayers.toIntOrNull() ?: 11)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (uiState.hasCommonPlayer) {
                                    val totalW = maxW + 1
                                    "$totalW Batters • $totalW Wkts (${maxW}+1 CMN)"
                                } else {
                                    "$displayPlayers Players • $maxW Wickets (Gully Rules)"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips Row: 5, 6, 7, 8, 11, Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 6, 7, 8, 11).forEach { count ->
                            FilterChip(
                                selected = !uiState.isCustomPlayersSelected && uiState.playersPerTeam == count,
                                onClick = { viewModel.onPresetPlayersSelected(count) },
                                label = { Text("${count}s", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FilterChip(
                            selected = uiState.isCustomPlayersSelected,
                            onClick = { viewModel.onCustomPlayersSelected() },
                            label = { Text("Custom", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    if (uiState.isCustomPlayersSelected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Team size (2-30):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { viewModel.decrementPlayers() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }

                                OutlinedTextField(
                                    value = uiState.customPlayersText,
                                    onValueChange = { viewModel.onCustomPlayersTextChange(it) },
                                    modifier = Modifier.width(64.dp),
                                    textStyle = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                FilledTonalIconButton(
                                    onClick = { viewModel.incrementPlayers() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // Common Player Slot (Gully Cricket Rule)
                    // ---------------------------------------------------------
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CommonPlayerBadge()
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Common Player Slot (Odd Total)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Plays, bats & bowls for both teams",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.hasCommonPlayer,
                            onCheckedChange = { viewModel.onHasCommonPlayerChange(it) }
                        )
                    }

                    if (uiState.hasCommonPlayer) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PlayerAutocompleteTextField(
                            value = uiState.commonPlayerName,
                            onValueChange = { viewModel.onCommonPlayerNameChange(it) },
                            label = "Common Player Name *",
                            placeholder = "e.g. Ramesh",
                            allProfiles = uiState.allProfiles,
                            onRequestAddProfile = { viewModel.onRequestAddProfile(it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val baseCount = uiState.playersPerTeam
                        val oddTotal = (baseCount * 2) + 1
                        val teamBatters = baseCount + 1

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CommonPlayerBlueContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CommonPlayerBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ðŸ Gully Rule: $oddTotal total players (odd number). ${uiState.commonPlayerName.ifBlank { "Common Player" }} will bat and bowl for both teams! ($teamBatters wickets per side)",
                                    color = CommonPlayerBlueText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Section 3: Toss & Decision Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOSS & BATTING DECISION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Who won the toss?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.isTeamAWonToss,
                            onClick = { viewModel.setTossWinnerIsTeamA(true) },
                            label = { Text(teamAName, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (uiState.isTeamAWonToss) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = !uiState.isTeamAWonToss,
                            onClick = { viewModel.setTossWinnerIsTeamA(false) },
                            label = { Text(teamBName, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (!uiState.isTeamAWonToss) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Elected to:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.tossDecision == "bat",
                            onClick = { viewModel.onTossDecisionChange("bat") },
                            label = { Text("ðŸ Bat First", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = uiState.tossDecision == "bowl",
                            onClick = { viewModel.onTossDecisionChange("bowl") },
                            label = { Text("âš¾ Bowl First", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val winnerName = if (uiState.isTeamAWonToss) teamAName else teamBName
                        val battingTeam = if (uiState.tossDecision == "bat") {
                            winnerName
                        } else {
                            if (uiState.isTeamAWonToss) teamBName else teamAName
                        }
                        Text(
                            text = "ðŸ‘‰ $battingTeam will bat in the 1st Innings.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // Section 4: Match Date Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MATCH DATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        Surface(
                            onClick = {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                viewModel.onDateChange(today)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = { viewModel.onDateChange(it) },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // -------------------------------------------------------------
            // Section 5: Opening Players Card
            // -------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPENING PLAYERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        Surface(
                            onClick = { viewModel.setShowDirectoryDialog(true) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Profiles (${uiState.allProfiles.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PlayerAutocompleteTextField(
                        value = uiState.strikerName,
                        onValueChange = { viewModel.onStrikerNameChange(it) },
                        label = "Striker (Facing ball 1) *",
                        placeholder = "Type name (e.g. Rohit)",
                        allProfiles = uiState.allProfiles,
                        onRequestAddProfile = { viewModel.onRequestAddProfile(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PlayerAutocompleteTextField(
                        value = uiState.nonStrikerName,
                        onValueChange = { viewModel.onNonStrikerNameChange(it) },
                        label = "Non-Striker (Other end) *",
                        placeholder = "Type name (e.g. Virat)",
                        allProfiles = uiState.allProfiles,
                        onRequestAddProfile = { viewModel.onRequestAddProfile(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PlayerAutocompleteTextField(
                        value = uiState.bowlerName,
                        onValueChange = { viewModel.onBowlerNameChange(it) },
                        label = "Opening Bowler (Over 1) *",
                        placeholder = "Type name (e.g. Bumrah)",
                        allProfiles = uiState.allProfiles,
                        onRequestAddProfile = { viewModel.onRequestAddProfile(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error Display
            if (uiState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // -------------------------------------------------------------
            // Submit Button
            // -------------------------------------------------------------
            Button(
                onClick = { viewModel.createMatch(onMatchCreated) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Scoring Match",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog for adding / editing profile photo for any player
    if (uiState.editingProfileForPlayer != null) {
        val editingName = uiState.editingProfileForPlayer ?: ""
        val currentProf = uiState.allProfiles.find { it.name.equals(editingName, ignoreCase = true) }
        AddProfilePhotoDialog(
            playerName = editingName,
            currentProfile = currentProf,
            onDismiss = { viewModel.dismissAddProfileDialog() },
            onPhotoSelected = { uri -> viewModel.onPhotoSelected(editingName, uri) },
            onAvatarPresetSelected = { icon -> viewModel.onAvatarPresetSelected(editingName, icon) },
            onRemovePhoto = { viewModel.onRemovePhoto(editingName) }
        )
    }

    // Dialog for viewing directory of all player profiles
    PlayerProfilesDirectoryDialog(
        isOpen = uiState.showDirectoryDialog,
        onDismiss = { viewModel.setShowDirectoryDialog(false) },
        profiles = uiState.allProfiles,
        onRequestAddProfile = { viewModel.onRequestAddProfile(it) },
        onAddNewPlayer = { viewModel.onAddNewPlayerToDirectory(it) }
    )
}
