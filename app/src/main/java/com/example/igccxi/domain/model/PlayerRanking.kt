package com.example.igccxi.domain.model

import java.util.Locale

/**
 * Aggregated batting statistics and ranking for a unique batsman across all scorecards.
 */
data class PlayerBattingRanking(
    val rank: Int = 0,
    val name: String = "",
    val inningsCount: Int = 0,
    val totalRuns: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val highestScore: Int = 0,
    val isHighestScoreNotOut: Boolean = false,
    val timesOut: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0
) {
    val highestScoreFormatted: String
        get() = "$highestScore${if (isHighestScoreNotOut) "*" else ""}"

    val strikeRate: Double
        get() = if (ballsFaced > 0) (totalRuns.toDouble() * 100.0) / ballsFaced else 0.0

    val strikeRateFormatted: String
        get() = String.format(Locale.US, "%.1f", strikeRate)

    val average: Double
        get() = if (timesOut > 0) totalRuns.toDouble() / timesOut else totalRuns.toDouble()

    val averageFormatted: String
        get() = String.format(Locale.US, "%.1f", average)
}

/**
 * Aggregated bowling statistics and ranking for a unique bowler across all scorecards.
 */
data class PlayerBowlingRanking(
    val rank: Int = 0,
    val name: String = "",
    val inningsCount: Int = 0,
    val totalBalls: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0
) {
    val oversFormatted: String
        get() = "${totalBalls / 6}.${totalBalls % 6}"

    val economyRate: Double
        get() = if (totalBalls > 0) (runsConceded.toDouble() * 6.0) / totalBalls else 0.0

    val economyFormatted: String
        get() = String.format(Locale.US, "%.2f", economyRate)

    val bowlingAverage: Double
        get() = if (wickets > 0) runsConceded.toDouble() / wickets else 0.0

    val bowlingAverageFormatted: String
        get() = if (wickets > 0) String.format(Locale.US, "%.1f", bowlingAverage) else "-"

    val bestBowlingFormatted: String
        get() = if (bestBowlingWickets > 0 || bestBowlingRuns > 0) "$bestBowlingWickets/$bestBowlingRuns" else "-"
}

/**
 * Combined container for batsmen and bowler leaderboards.
 */
data class RankingsData(
    val batsmen: List<PlayerBattingRanking> = emptyList(),
    val bowlers: List<PlayerBowlingRanking> = emptyList(),
    val totalMatchesAnalyzed: Int = 0
)
