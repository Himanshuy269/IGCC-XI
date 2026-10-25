package com.example.igccxi.data.repository

import com.example.igccxi.data.model.BallEvent
import com.example.igccxi.data.model.BallType
import com.example.igccxi.data.model.BatterScore
import com.example.igccxi.data.model.BowlerScore
import com.example.igccxi.data.model.Extras
import com.example.igccxi.data.model.Innings
import com.example.igccxi.data.model.Match
import com.example.igccxi.data.model.MatchStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InMemoryMatchRepository(
    private val localUserId: String = "local_scorer_" + UUID.randomUUID().toString().take(6)
) : MatchRepository {

    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val initialMatches = mutableListOf(
        createSampleLiveMatch(),
        createSampleCompletedMatch()
    )

    private val _matchesFlow = MutableStateFlow<List<Match>>(initialMatches)

    override fun observeMatches(): Flow<List<Match>> = _matchesFlow

    override fun observeMatch(matchId: String): Flow<Match?> {
        return _matchesFlow.map { list -> list.find { it.id == matchId } }
    }

    override suspend fun createMatch(match: Match): String {
        val id = if (match.id.isNotEmpty()) match.id else "m_" + UUID.randomUUID().toString().take(8)
        val newMatch = match.copy(
            id = id,
            scorerId = localUserId
        )
        val updated = listOf(newMatch) + _matchesFlow.value
        _matchesFlow.value = updated
        return id
    }

    override suspend fun updateMatch(match: Match) {
        val currentList = _matchesFlow.value
        val updatedList = currentList.map { if (it.id == match.id) match else it }
        _matchesFlow.value = updatedList
    }

    override suspend fun getCurrentUserId(): String = localUserId

    private fun createSampleLiveMatch(): Match {
        val striker = BatterScore(id = "b1", name = "V. Kohli", runs = 48, ballsFaced = 32, fours = 5, sixes = 1)
        val nonStriker = BatterScore(id = "b2", name = "S. Yadav", runs = 24, ballsFaced = 14, fours = 2, sixes = 2)
        val outBatter1 = BatterScore(id = "b0", name = "R. Sharma", runs = 35, ballsFaced = 22, fours = 4, sixes = 1, isOut = true, dismissal = "c Warner b Starc")
        val outBatter2 = BatterScore(id = "b01", name = "S. Gill", runs = 18, ballsFaced = 12, fours = 3, sixes = 0, isOut = true, dismissal = "b Cummins")

        val bowler1 = BowlerScore(id = "bw1", name = "M. Starc", oversBowled = 3, ballsBowledInOver = 2, runsConceded = 28, wickets = 1)
        val bowler2 = BowlerScore(id = "bw2", name = "P. Cummins", oversBowled = 4, ballsBowledInOver = 0, runsConceded = 36, wickets = 1)
        val bowler3 = BowlerScore(id = "bw3", name = "A. Zampa", oversBowled = 3, ballsBowledInOver = 0, runsConceded = 24, wickets = 0)

        val recentBalls = listOf(
            BallEvent(id = "e1", displayText = "1", runsOffBat = 1),
            BallEvent(id = "e2", displayText = "4", runsOffBat = 4),
            BallEvent(id = "e3", displayText = "0", runsOffBat = 0),
            BallEvent(id = "e4", displayText = "Wd", extraRuns = 1, type = BallType.WIDE, isLegalBall = false),
            BallEvent(id = "e5", displayText = "6", runsOffBat = 6),
            BallEvent(id = "e6", displayText = "1", runsOffBat = 1)
        )

        val inn1 = Innings(
            inningsNumber = 1,
            battingTeam = "India",
            bowlingTeam = "Australia",
            totalRuns = 132,
            wickets = 2,
            oversBowled = 14,
            ballsBowledInOver = 2,
            currentStrikerId = striker.id,
            currentNonStrikerId = nonStriker.id,
            currentBowlerId = bowler1.id,
            batters = listOf(outBatter1, outBatter2, striker, nonStriker),
            bowlers = listOf(bowler1, bowler2, bowler3),
            extras = Extras(wides = 4, noBalls = 1, byes = 2, legByes = 0),
            recentBalls = recentBalls
        )

        return Match(
            id = "live_sample_1",
            teamA = "India",
            teamB = "Australia",
            date = todayDate,
            totalOvers = 20,
            status = MatchStatus.INNINGS_1,
            scorerId = localUserId,
            currentInningsNumber = 1,
            innings1 = inn1,
            tossWinner = "India",
            tossDecision = "bat"
        )
    }

    private fun createSampleCompletedMatch(): Match {
        val inn1Batters = listOf(
            BatterScore("e_b1", "J. Buttler", 64, 40, 6, 3, true, "c Klaasen b Rabada"),
            BatterScore("e_b2", "P. Salt", 28, 16, 4, 1, true, "b Nortje"),
            BatterScore("e_b3", "H. Brook", 52, 34, 4, 2, false, "not out"),
            BatterScore("e_b4", "L. Livingstone", 22, 12, 1, 2, false, "not out")
        )
        val inn1Bowlers = listOf(
            BowlerScore("e_bw1", "K. Rabada", 4, 0, 0, 38, 1),
            BowlerScore("e_bw2", "A. Nortje", 4, 0, 0, 42, 1),
            BowlerScore("e_bw3", "K. Maharaj", 4, 0, 0, 31, 0)
        )
        val inn1 = Innings(
            inningsNumber = 1,
            battingTeam = "England",
            bowlingTeam = "South Africa",
            totalRuns = 175,
            wickets = 2,
            oversBowled = 20,
            ballsBowledInOver = 0,
            isCompleted = true,
            batters = inn1Batters,
            bowlers = inn1Bowlers,
            extras = Extras(wides = 6, noBalls = 1, byes = 2, legByes = 0)
        )

        val inn2Batters = listOf(
            BatterScore("sa_b1", "Q. de Kock", 42, 28, 4, 2, true, "c Buttler b Archer"),
            BatterScore("sa_b2", "R. Hendricks", 14, 10, 2, 0, true, "b Curran"),
            BatterScore("sa_b3", "A. Markram", 35, 25, 3, 1, true, "c Brook b Rashid"),
            BatterScore("sa_b4", "D. Miller", 55, 30, 4, 3, false, "not out"),
            BatterScore("sa_b5", "H. Klaasen", 26, 14, 2, 2, false, "not out")
        )
        val inn2Bowlers = listOf(
            BowlerScore("eng_bw1", "J. Archer", 4, 0, 0, 35, 1),
            BowlerScore("eng_bw2", "S. Curran", 3, 4, 0, 42, 1),
            BowlerScore("eng_bw3", "A. Rashid", 4, 0, 0, 32, 1)
        )
        val inn2 = Innings(
            inningsNumber = 2,
            battingTeam = "South Africa",
            bowlingTeam = "England",
            totalRuns = 178,
            wickets = 3,
            oversBowled = 18,
            ballsBowledInOver = 4,
            isCompleted = true,
            batters = inn2Batters,
            bowlers = inn2Bowlers,
            extras = Extras(wides = 5, noBalls = 1, byes = 0, legByes = 0)
        )

        return Match(
            id = "completed_sample_1",
            teamA = "England",
            teamB = "South Africa",
            date = todayDate,
            totalOvers = 20,
            status = MatchStatus.COMPLETED,
            scorerId = "other_scorer_123",
            currentInningsNumber = 2,
            innings1 = inn1,
            innings2 = inn2,
            target = 176,
            result = "South Africa won by 7 wickets",
            tossWinner = "South Africa",
            tossDecision = "bowl"
        )
    }
}
