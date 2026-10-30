package com.example.igccxi.screens.scoring.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.ui.theme.CricketExtraAmber
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import com.example.igccxi.ui.theme.CricketSixPurple
import com.example.igccxi.ui.theme.CricketWicketContainer
import com.example.igccxi.ui.theme.CricketWicketRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WicketDialog(
    existingBatterNames: List<String> = emptyList(),
    strikerName: String = "",
    nonStrikerName: String = "",
    currentBowlerName: String = "",
    existingBowlerNames: List<String> = emptyList(),
    currentWickets: Int = 0,
    maxWickets: Int = 10,
    isBattingAlone: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (dismissal: String, newBatterName: String, isStrikerOut: Boolean) -> Unit
) {
    val commonDismissals = listOf("Bowled", "Caught", "LBW", "Run Out", "Stumped", "Hit Wicket", "Boundary Paar")
    var selectedDismissal by remember { mutableStateOf("Bowled") }
    var bowlerName by remember(currentBowlerName) { mutableStateOf(currentBowlerName.trim()) }
    var fielderName by remember { mutableStateOf("") }
    var isCaughtAndBowled by remember { mutableStateOf(false) }
    var isStrikerOut by remember { mutableStateOf(true) }
    var newBatter by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val effectiveBowler = bowlerName.trim().ifEmpty { currentBowlerName.trim() }
    val cleanFielder = fielderName.trim()

    val constructedDismissal = when (selectedDismissal) {
        "Bowled" -> if (effectiveBowler.isNotEmpty()) "b $effectiveBowler" else "bowled"
        "Caught" -> {
            when {
                isCaughtAndBowled -> if (effectiveBowler.isNotEmpty()) "c & b $effectiveBowler" else "c & b"
                cleanFielder.isNotEmpty() && effectiveBowler.isNotEmpty() -> "c $cleanFielder b $effectiveBowler"
                cleanFielder.isNotEmpty() -> "c $cleanFielder"
                effectiveBowler.isNotEmpty() -> "caught b $effectiveBowler"
                else -> "caught"
            }
        }
        "LBW" -> if (effectiveBowler.isNotEmpty()) "lbw b $effectiveBowler" else "lbw"
        "Stumped" -> {
            when {
                cleanFielder.isNotEmpty() && effectiveBowler.isNotEmpty() -> "st $cleanFielder b $effectiveBowler"
                cleanFielder.isNotEmpty() -> "st $cleanFielder"
                effectiveBowler.isNotEmpty() -> "st b $effectiveBowler"
                else -> "stumped"
            }
        }
        "Hit Wicket" -> if (effectiveBowler.isNotEmpty()) "hit wicket b $effectiveBowler" else "hit wicket"
        "Run Out" -> {
            if (cleanFielder.isNotEmpty()) "run out ($cleanFielder)" else "run out"
        }
        "Boundary Paar" -> {
            if (effectiveBowler.isNotEmpty()) "boundary paar (b $effectiveBowler)" else "boundary paar"
        }
        else -> {
            if (effectiveBowler.isNotEmpty()) "${selectedDismissal.lowercase()} b $effectiveBowler" else selectedDismissal.lowercase()
        }
    }

    val isAllOutWicket = isBattingAlone || (currentWickets + 1 >= maxWickets)
    val isTransitionToLoneBatsman = !isAllOutWicket && (currentWickets + 1 == maxWickets - 1)
    val noNextBatterRequired = isAllOutWicket || isTransitionToLoneBatsman
    val cleanName = newBatter.trim()
    val isDuplicate = !noNextBatterRequired && cleanName.isNotEmpty() &&
        existingBatterNames.any { it.trim().equals(cleanName, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = CricketWicketContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = CricketWicketRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when {
                            isAllOutWicket -> "Record Final Wicket (All Out)"
                            isTransitionToLoneBatsman -> "Record Wicket #${currentWickets + 1} (Lone Batsman)"
                            else -> "Record Wicket / Dismissal"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Wicket #${currentWickets + 1} of $maxWickets in this innings",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Dismissal Mode:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonDismissals.forEach { type ->
                        FilterChip(
                            selected = selectedDismissal == type,
                            onClick = { selectedDismissal = type },
                            label = { Text(type, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CricketWicketContainer,
                                selectedLabelColor = CricketWicketRed
                            )
                        )
                    }
                }

                // If 2 batters at crease: allow selecting who was dismissed
                if (!isBattingAlone && nonStrikerName.isNotBlank()) {
                    Text(
                        text = if (selectedDismissal.equals("Run Out", ignoreCase = true)) "Who was Run Out?" else "Batsman Dismissed:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isStrikerOut,
                            onClick = { isStrikerOut = true },
                            label = { Text("${strikerName.ifEmpty { "Striker" }} (Striker)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isStrikerOut,
                            onClick = { isStrikerOut = false },
                            label = { Text("${nonStrikerName.ifEmpty { "Non-Striker" }} (Non-Striker)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val isRunOut = selectedDismissal.equals("Run Out", ignoreCase = true)
                val isBoundaryPaar = selectedDismissal.equals("Boundary Paar", ignoreCase = true)
                val isCaught = selectedDismissal.equals("Caught", ignoreCase = true)
                val isStumped = selectedDismissal.equals("Stumped", ignoreCase = true)

                // Bowler section (for all dismissals where bowler is credited)
                if (!isRunOut) {
                    OutlinedTextField(
                        value = bowlerName,
                        onValueChange = { bowlerName = it },
                        label = { Text("Bowler (Taking Wicket)") },
                        placeholder = { Text("e.g. Bumrah") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (existingBowlerNames.size > 1) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            existingBowlerNames.forEach { bName ->
                                FilterChip(
                                    selected = bowlerName.trim().equals(bName.trim(), ignoreCase = true),
                                    onClick = { bowlerName = bName },
                                    label = { Text(bName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                if (isCaught) {
                    FilterChip(
                        selected = isCaughtAndBowled,
                        onClick = { isCaughtAndBowled = !isCaughtAndBowled },
                        label = { Text("Caught & Bowled (c & b $effectiveBowler)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )

                    if (!isCaughtAndBowled) {
                        OutlinedTextField(
                            value = fielderName,
                            onValueChange = { fielderName = it },
                            label = { Text("Catcher / Fielder Name (Optional)") },
                            placeholder = { Text("e.g. Kohli") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (isStumped) {
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        label = { Text("Wicketkeeper Name (Optional)") },
                        placeholder = { Text("e.g. Dhoni") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (isRunOut) {
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        label = { Text("Fielder / Thrower (Optional)") },
                        placeholder = { Text("e.g. Jadeja") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (isBoundaryPaar) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🏏 Gully Rule: Ball hit directly outside or over boundary wall is OUT! Wicket credited to $effectiveBowler.",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Dismissal Live Preview Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CricketWicketContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CricketWicketRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val dismissedName = if (isStrikerOut) strikerName.ifEmpty { "Striker" } else nonStrikerName.ifEmpty { "Non-Striker" }
                        Text(
                            text = "🔴 OUT: $dismissedName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CricketWicketRed
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scorecard notation: $constructedDismissal",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (isAllOutWicket) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CricketWicketContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎯 Final Wicket: Innings will be All Out ($maxWickets wickets down). No next batter required.",
                            color = CricketWicketRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else if (isTransitionToLoneBatsman) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Gully Cricket Rule: All $maxWickets players have batted. The remaining not-out batter will continue batting alone until the final ${maxWickets}th wicket.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = newBatter,
                        onValueChange = {
                            newBatter = it
                            validationError = null
                        },
                        label = { Text("Next Batter Name") },
                        placeholder = { Text("e.g. Next Batter") },
                        singleLine = true,
                        isError = isDuplicate || validationError != null,
                        supportingText = {
                            if (isDuplicate) {
                                Text(
                                    text = "⚠️ '$cleanName' has already batted or is at crease in this innings",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            } else if (validationError != null) {
                                Text(
                                    text = validationError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isDuplicate) {
                        validationError = "Duplicate player name not allowed in same innings"
                        return@Button
                    }
                    val nextName = if (noNextBatterRequired) "" else cleanName
                    val effectiveOut = if (isBattingAlone) true else isStrikerOut
                    onConfirm(constructedDismissal, nextName, effectiveOut)
                },
                enabled = !isDuplicate,
                colors = ButtonDefaults.buttonColors(containerColor = CricketWicketRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = when {
                        isAllOutWicket -> "Confirm All Out"
                        isTransitionToLoneBatsman -> "Confirm (Lone Batsman)"
                        else -> "Confirm Out"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExtrasDialog(
    onDismiss: () -> Unit,
    onBye: (runs: Int) -> Unit,
    onLegBye: (runs: Int) -> Unit,
    onWide: (extraRuns: Int) -> Unit,
    onNoBall: (runsOffBat: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsCricket,
                    contentDescription = null,
                    tint = CricketExtraAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Extras (Cricket Laws)", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 1. Byes (Law 23)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Byes (Runs without bat/body contact - Bowler not charged):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 4).forEach { runs ->
                            OutlinedButton(
                                onClick = { onBye(runs) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("${runs}B", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 2. Leg Byes (Law 24)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Leg Byes (Runs off batsman body - Bowler not charged):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 4).forEach { runs ->
                            OutlinedButton(
                                onClick = { onLegBye(runs) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("${runs}Lb", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 3. Wides with runs (Law 22)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Wide with extra runs / boundary:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1 to "+1 Wd", 2 to "+2 Wd", 3 to "+3 Wd", 5 to "+5 Wd (4byes)").forEach { (runs, label) ->
                            OutlinedButton(
                                onClick = { onWide(runs) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // 4. No Ball with runs off bat (Law 21)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "No Ball + Runs Scored off Bat (Free hit follows):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0 to "Nb+0", 1 to "Nb+1", 2 to "Nb+2", 4 to "Nb+4", 6 to "Nb+6").forEach { (runs, label) ->
                            Button(
                                onClick = { onNoBall(runs) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (runs >= 4) CricketSixPurple else Color(0xFFEA580C)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BowlerDialog(
    existingBowlers: List<String>,
    currentBowlerName: String,
    isOverComplete: Boolean = false,
    overNumber: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (bowlerName: String) -> Unit
) {
    var bowlerName by remember { mutableStateOf("") }
    val previousBowler = currentBowlerName.trim()
    val isSameConsecutiveBowler = isOverComplete && bowlerName.trim().isNotEmpty() &&
        bowlerName.trim().equals(previousBowler, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChangeCircle,
                    contentDescription = null,
                    tint = if (isOverComplete) CricketGold else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOverComplete) "Over $overNumber Complete! 🎯" else "Select Bowler",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    if (isOverComplete) {
                        Text(
                            text = "Choose bowler for Over ${overNumber + 1}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (existingBowlers.isNotEmpty()) {
                    Text(
                        text = if (isOverComplete) "Pick another bowler from team:" else "Pick previous bowler:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingBowlers.forEach { name ->
                            val isJustBowled = isOverComplete && name.trim().equals(previousBowler, ignoreCase = true)
                            FilterChip(
                                selected = bowlerName.trim().equals(name.trim(), ignoreCase = true),
                                onClick = { bowlerName = name },
                                enabled = !isJustBowled,
                                label = {
                                    Text(
                                        text = if (isJustBowled) "$name (Just Bowled)" else name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = bowlerName,
                    onValueChange = { bowlerName = it },
                    label = { Text("Or Type New Bowler Name") },
                    placeholder = { Text("e.g. Jasprit Bumrah") },
                    singleLine = true,
                    isError = isSameConsecutiveBowler,
                    supportingText = {
                        if (isSameConsecutiveBowler) {
                            Text(
                                text = "Rule: $previousBowler just bowled over $overNumber and cannot bowl two consecutive overs.",
                                color = CricketWicketRed,
                                fontSize = 11.sp
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bowlerName.trim().isNotEmpty() && !isSameConsecutiveBowler) {
                        onConfirm(bowlerName.trim())
                    }
                },
                enabled = bowlerName.trim().isNotEmpty() && !isSameConsecutiveBowler,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isOverComplete) "Set Bowler for Over ${overNumber + 1}" else "Set Bowler",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isOverComplete) "Later" else "Cancel")
            }
        }
    )
}

@Composable
fun SecondInningsDialog(
    battingTeam: String,
    onDismiss: () -> Unit,
    onConfirm: (striker: String, nonStriker: String, bowler: String) -> Unit
) {
    var striker by remember { mutableStateOf("") }
    var nonStriker by remember { mutableStateOf("") }
    var bowler by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val s = striker.trim().ifEmpty { "Chasing Striker" }
    val ns = nonStriker.trim().ifEmpty { "Chasing Non-Striker" }
    val isDuplicate = striker.trim().isNotEmpty() &&
        nonStriker.trim().isNotEmpty() &&
        s.equals(ns, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Start $battingTeam Innings", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter opening batters and opening bowler for 2nd Innings:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = striker,
                    onValueChange = {
                        striker = it
                        errorMessage = null
                    },
                    label = { Text("Opening Striker") },
                    placeholder = { Text("e.g. Batter 1") },
                    singleLine = true,
                    isError = isDuplicate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nonStriker,
                    onValueChange = {
                        nonStriker = it
                        errorMessage = null
                    },
                    label = { Text("Opening Non-Striker") },
                    placeholder = { Text("e.g. Batter 2") },
                    singleLine = true,
                    isError = isDuplicate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isDuplicate || errorMessage != null) {
                    Text(
                        text = errorMessage ?: "⚠️ Striker and non-striker cannot have the same player name",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = bowler,
                    onValueChange = { bowler = it },
                    label = { Text("Opening Bowler") },
                    placeholder = { Text("e.g. Bowler 1") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (s.equals(ns, ignoreCase = true)) {
                        errorMessage = "Striker and non-striker cannot have the same player name"
                        return@Button
                    }
                    onConfirm(striker, nonStriker, bowler)
                },
                enabled = !isDuplicate,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start Chasing", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditOversDialog(
    currentOvers: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var oversText by remember { mutableStateOf(currentOvers.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Revise Match Overs", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Adjust the total overs for this match (e.g. rain reduction or revised match length):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = oversText,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                        oversText = filtered
                        error = null
                    },
                    label = { Text("Total Match Overs") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 8, 10, 15, 20).forEach { ov ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { oversText = ov.toString() }
                        ) {
                            Text(
                                text = "$ov ov",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = oversText.toIntOrNull()
                    if (parsed == null || parsed < 1 || parsed > 100) {
                        error = "Please enter valid overs between 1 and 100"
                    } else {
                        onConfirm(parsed)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Update Overs", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
