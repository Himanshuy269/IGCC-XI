package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.PlayerBattingRanking
import com.example.igccxi.domain.model.PlayerBowlingRanking
import com.example.igccxi.domain.model.RankingsData
import com.example.igccxi.domain.repository.MatchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * ====================================================================================
 * CLEAN ARCHITECTURE: USE CASE LAYER (Domain Business Logic)
 * ====================================================================================
 *
 * [GetPlayerRankingsUseCase] is responsible for:
 * 1. Listening reactively to the stream of all matches from [MatchRepository].
 * 2. Aggregating all innings, batters, and bowlers into official leaderboards.
 * 3. Enforcing single-entry deduplication by player name (case-insensitive).
 * 4. Offloading heavy numerical calculations to [Dispatchers.Default] so the Android
 *    UI thread remains completely free for 60/120fps buttery smooth rendering.
 *
 * JUNIOR DEVELOPER NOTE ON COROUTINES & FLOW:
 * - A Kotlin [Flow] is a cold asynchronous data stream that emits multiple values sequentially.
 * - `.map { ... }` transforms each incoming match list into an aggregated [RankingsData].
 * - `.flowOn(Dispatchers.Default)` guarantees that the CPU-intensive aggregation loop
 *   executes on a background worker thread rather than on the Main (UI) thread.
 *
 * @param repository The data repository supplying the reactive match stream.
 * @param defaultDispatcher The coroutine dispatcher for CPU-bound computations (defaults to [Dispatchers.Default]).
 */
class GetPlayerRankingsUseCase(
    private val repository: MatchRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    companion object {
        // Known test matches from early app testing that are excluded from public stats
        private val TEST_MATCH_IDS = setOf(
            "F2A7oeEcJkZ6ZS2z4aoF", // 123 vs 456
            "BqSQumBqWYACyUNqUejC",
            "ByHqjCGGs6PIMoAJeMM8",
            "bas5xgJbDcttspMMANS6",
            "bs6wIYJzSaCKcbe2x0gt",
            "l2g2C6DZc4nkbqcqM2h2",
            "sRS9jGq0kh6jZpETkHTO",
            "zWfTJTNutdURFHKEmrjh"
        )
    }

    /**
     * Determines whether a given player in a match is eligible to be counted in rankings.
     * Prevents early test match artifacts while preserving valid players like Kashish.
     */
    private fun isEligiblePlayer(name: String, match: Match): Boolean {
        val clean = name.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return false

        // Kashish is explicitly preserved
        if (clean == "kashish") return true

        // For all test matches, remove all players except Kashish
        if (match.id in TEST_MATCH_IDS ||
            (match.teamA.trim().equals("123", ignoreCase = true) && match.teamB.trim().equals("456", ignoreCase = true))
        ) {
            return false
        }

        return true
    }

    /**
     * Executes the Use Case reactively.
     * Whenever matches update in Firestore or local storage, this Flow emits
     * freshly computed and sorted [RankingsData].
     */
    operator fun invoke(): Flow<RankingsData> {
        return repository.observeMatches()
            .map { matches ->
                calculateRankings(matches)
            }
            .flowOn(defaultDispatcher) // Keep CPU-heavy loops off the Main/UI thread
    }

    /**
     * Pure business logic method that transforms raw [Match] scorecards into sorted leaderboards.
     * Being a pure function makes it 100% testable without mocking Android frameworks!
     */
    fun calculateRankings(matches: List<Match>): RankingsData {
        val battingMap = mutableMapOf<String, MutableBattingStats>()
        val bowlingMap = mutableMapOf<String, MutableBowlingStats>()

        // Exclude match "123 vs 456"
        val eligibleMatches = matches.filterNot {
            it.id == "F2A7oeEcJkZ6ZS2z4aoF" ||
            (it.teamA.trim().equals("123", ignoreCase = true) && it.teamB.trim().equals("456", ignoreCase = true))
        }

        var matchesAnalyzed = 0

        for (match in eligibleMatches) {
            val inningsList = listOfNotNull(match.innings1, match.innings2)
            if (inningsList.isNotEmpty()) {
                matchesAnalyzed++
            }

            for (innings in inningsList) {
                // 1. Process Batters in this innings
                for (batter in innings.batters) {
                    val rawName = batter.name.trim()
                    if (rawName.isEmpty() || !isEligiblePlayer(rawName, match)) continue

                    val key = rawName.lowercase(Locale.ROOT)
                    val stats = battingMap.getOrPut(key) {
                        MutableBattingStats(canonicalName = formatPlayerName(rawName))
                    }

                    stats.inningsCount++
                    stats.totalRuns += batter.runs
                    stats.ballsFaced += batter.ballsFaced
                    stats.fours += batter.fours
                    stats.sixes += batter.sixes

                    if (batter.isOut) {
                        stats.timesOut++
                    }

                    // Track highest score
                    if (batter.runs > stats.highestScore || (batter.runs == stats.highestScore && !batter.isOut)) {
                        stats.highestScore = batter.runs
                        stats.isHighestScoreNotOut = !batter.isOut
                    }

                    if (batter.runs >= 100) {
                        stats.hundreds++
                    } else if (batter.runs >= 50) {
                        stats.fifties++
                    }
                }

                // 2. Process Bowlers in this innings
                for (bowler in innings.bowlers) {
                    val rawName = bowler.name.trim()
                    if (rawName.isEmpty() || !isEligiblePlayer(rawName, match)) continue

                    val balls = bowler.totalBalls
                    // Only count spells where bowler actually bowled or conceded/took wickets
                    if (balls == 0 && bowler.runsConceded == 0 && bowler.wickets == 0) continue

                    val key = rawName.lowercase(Locale.ROOT)
                    val stats = bowlingMap.getOrPut(key) {
                        MutableBowlingStats(canonicalName = formatPlayerName(rawName))
                    }

                    stats.inningsCount++
                    stats.totalBalls += balls
                    stats.maidens += bowler.maidens
                    stats.runsConceded += bowler.runsConceded
                    stats.wickets += bowler.wickets

                    // Track best bowling figures (most wickets; fewest runs if equal wickets)
                    val isBetterSpell = when {
                        bowler.wickets > stats.bestWickets -> true
                        bowler.wickets == stats.bestWickets && bowler.runsConceded < stats.bestRuns -> true
                        else -> false
                    }
                    if (isBetterSpell) {
                        stats.bestWickets = bowler.wickets
                        stats.bestRuns = bowler.runsConceded
                    }
                }
            }
        }

        // ============================================================================
        // STEP 4: SORTING & GENERATING OFFICIAL RANKING POSITIONS
        // ============================================================================
        // Sort Batsmen by:
        //   1. Total Runs (descending)
        //   2. Highest Score (descending)
        //   3. Strike Rate (descending)
        val sortedBatsmen = battingMap.values
            .filter { it.inningsCount > 0 || it.totalRuns > 0 }
            .sortedWith(
                compareByDescending<MutableBattingStats> { it.totalRuns }
                    .thenByDescending { it.highestScore }
                    .thenByDescending { if (it.ballsFaced > 0) (it.totalRuns.toDouble() * 100.0) / it.ballsFaced else 0.0 }
            )
            .mapIndexed { index, item ->
                PlayerBattingRanking(
                    rank = index + 1,
                    name = item.canonicalName,
                    inningsCount = item.inningsCount,
                    totalRuns = item.totalRuns,
                    ballsFaced = item.ballsFaced,
                    fours = item.fours,
                    sixes = item.sixes,
                    highestScore = item.highestScore,
                    isHighestScoreNotOut = item.isHighestScoreNotOut,
                    timesOut = item.timesOut,
                    fifties = item.fifties,
                    hundreds = item.hundreds
                )
            }

        // Sort Bowlers by:
        //   1. Wickets (descending)
        //   2. Economy Rate (ascending - lower is better)
        //   3. Runs Conceded (ascending - fewer runs conceded breaks ties)
        val sortedBowlers = bowlingMap.values
            .filter { it.totalBalls > 0 || it.wickets > 0 }
            .sortedWith(
                compareByDescending<MutableBowlingStats> { it.wickets }
                    .thenBy { if (it.totalBalls > 0) (it.runsConceded.toDouble() * 6.0) / it.totalBalls else 999.0 }
                    .thenBy { it.runsConceded }
            )
            .mapIndexed { index, item ->
                PlayerBowlingRanking(
                    rank = index + 1,
                    name = item.canonicalName,
                    inningsCount = item.inningsCount,
                    totalBalls = item.totalBalls,
                    maidens = item.maidens,
                    runsConceded = item.runsConceded,
                    wickets = item.wickets,
                    bestBowlingWickets = item.bestWickets,
                    bestBowlingRuns = item.bestRuns
                )
            }

        return RankingsData(
            batsmen = sortedBatsmen,
            bowlers = sortedBowlers,
            totalMatchesAnalyzed = matchesAnalyzed
        )
    }

    private fun formatPlayerName(name: String): String {
        return name.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
                }
            }
    }

    private class MutableBattingStats(
        val canonicalName: String,
        var inningsCount: Int = 0,
        var totalRuns: Int = 0,
        var ballsFaced: Int = 0,
        var fours: Int = 0,
        var sixes: Int = 0,
        var highestScore: Int = 0,
        var isHighestScoreNotOut: Boolean = false,
        var timesOut: Int = 0,
        var fifties: Int = 0,
        var hundreds: Int = 0
    )

    private class MutableBowlingStats(
        val canonicalName: String,
        var inningsCount: Int = 0,
        var totalBalls: Int = 0,
        var maidens: Int = 0,
        var runsConceded: Int = 0,
        var wickets: Int = 0,
        var bestWickets: Int = 0,
        var bestRuns: Int = Int.MAX_VALUE
    )
}
