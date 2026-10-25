package com.example.igccxi.data.model

import java.util.Locale

enum class BallType {
    NORMAL,
    WIDE,
    NO_BALL,
    WICKET
}

data class BallEvent(
    val id: String = "",
    val type: BallType = BallType.NORMAL,
    val runsOffBat: Int = 0,
    val extraRuns: Int = 0,
    val isLegalBall: Boolean = true,
    val isWicket: Boolean = false,
    val batterId: String = "",
    val bowlerId: String = "",
    val displayText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Extras(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0
) {
    val total: Int
        get() = wides + noBalls + byes + legByes
}

data class BatterScore(
    val id: String = "",
    val name: String = "",
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissal: String = "" // "b Bowler", "c Fielder b Bowler", "run out", "not out"
) {
    val strikeRate: Double
        get() = if (ballsFaced > 0) (runs.toDouble() * 100.0) / ballsFaced else 0.0

    val strikeRateFormatted: String
        get() = String.format(Locale.US, "%.1f", strikeRate)
}

data class BowlerScore(
    val id: String = "",
    val name: String = "",
    val oversBowled: Int = 0,
    val ballsBowledInOver: Int = 0, // 0..5
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0
) {
    val oversFormatted: String
        get() = "$oversBowled.$ballsBowledInOver"

    val totalBalls: Int
        get() = (oversBowled * 6) + ballsBowledInOver

    val economyRate: Double
        get() = if (totalBalls > 0) (runsConceded.toDouble() * 6.0) / totalBalls else 0.0

    val economyFormatted: String
        get() = String.format(Locale.US, "%.2f", economyRate)
}

data class Innings(
    val inningsNumber: Int = 1,
    val battingTeam: String = "",
    val bowlingTeam: String = "",
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val oversBowled: Int = 0,
    val ballsBowledInOver: Int = 0, // 0 to 5
    val isCompleted: Boolean = false,
    val currentStrikerId: String = "",
    val currentNonStrikerId: String = "",
    val currentBowlerId: String = "",
    val batters: List<BatterScore> = emptyList(),
    val bowlers: List<BowlerScore> = emptyList(),
    val extras: Extras = Extras(),
    val recentBalls: List<BallEvent> = emptyList() // recent balls for ticker & undo
) {
    val totalLegalBalls: Int
        get() = (oversBowled * 6) + ballsBowledInOver

    val oversFormatted: String
        get() = "$oversBowled.$ballsBowledInOver"

    val currentRunRate: Double
        get() = if (totalLegalBalls > 0) (totalRuns.toDouble() * 6.0) / totalLegalBalls else 0.0

    val currentRunRateFormatted: String
        get() = String.format(Locale.US, "%.2f", currentRunRate)

    fun getStriker(): BatterScore? = batters.find { it.id == currentStrikerId }
    fun getNonStriker(): BatterScore? = batters.find { it.id == currentNonStrikerId }
    fun getCurrentBowler(): BowlerScore? = bowlers.find { it.id == currentBowlerId }
}

data class Match(
    val id: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val date: String = "", // Format: "YYYY-MM-DD" for easy filtering
    val totalOvers: Int = 20,
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val scorerId: String = "",
    val currentInningsNumber: Int = 1,
    val innings1: Innings = Innings(),
    val innings2: Innings? = null,
    val target: Int? = null,
    val result: String = "",
    val tossWinner: String = "",
    val tossDecision: String = "", // "bat" or "bowl"
    val createdAt: Long = System.currentTimeMillis()
) {
    val currentInnings: Innings
        get() = if (currentInningsNumber == 2 && innings2 != null) innings2 else innings1

    val isScorable: Boolean
        get() = status == MatchStatus.INNINGS_1 || status == MatchStatus.INNINGS_2
}
