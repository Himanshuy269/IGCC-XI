package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.BatterScore
import com.example.igccxi.domain.model.BowlerScore
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPlayerRankingsUseCaseTest {

    private lateinit var repository: FakeMatchRepository
    private lateinit var getPlayerRankingsUseCase: GetPlayerRankingsUseCase

    @Before
    fun setUp() {
        repository = FakeMatchRepository()
        getPlayerRankingsUseCase = GetPlayerRankingsUseCase(repository)
    }

    @Test
    fun testEmptyMatchesReturnsEmptyRankings() = runBlocking {
        val rankings = getPlayerRankingsUseCase().first()
        assertEquals(0, rankings.totalMatchesAnalyzed)
        assertTrue(rankings.batsmen.isEmpty())
        assertTrue(rankings.bowlers.isEmpty())
    }

    @Test
    fun testStrictDeduplicationAcrossMatchesAndInnings() = runBlocking {
        // Match 1: Player "Virat Kohli" scores 70 (40) and bowls 12 balls for 15 runs and 1 wicket
        val inn1 = Innings(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            batters = listOf(
                BatterScore(id = "b1", name = "Virat Kohli", runs = 70, ballsFaced = 40, fours = 8, sixes = 2, isOut = true),
                BatterScore(id = "b2", name = "Rohit Sharma", runs = 30, ballsFaced = 20, fours = 4, sixes = 1, isOut = false)
            ),
            bowlers = listOf(
                BowlerScore(id = "bw1", name = "Jasprit Bumrah", oversBowled = 2, ballsBowledInOver = 0, runsConceded = 12, wickets = 2)
            )
        )
        val match1 = Match(id = "m1", teamA = "Team A", teamB = "Team B", status = MatchStatus.COMPLETED, innings1 = inn1)
        repository.createMatch(match1)

        // Match 2: Player enters with different casing & spacing: "virat kohli " scores 50 (30)
        // Rohit enters as "ROHIT SHARMA" and scores 80 (45)
        val inn2 = Innings(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team C",
            batters = listOf(
                BatterScore(id = "b3", name = "virat kohli ", runs = 50, ballsFaced = 30, fours = 5, sixes = 1, isOut = true),
                BatterScore(id = "b4", name = "ROHIT SHARMA", runs = 80, ballsFaced = 45, fours = 7, sixes = 3, isOut = true)
            ),
            bowlers = listOf(
                BowlerScore(id = "bw2", name = "jasprit bumrah", oversBowled = 2, ballsBowledInOver = 0, runsConceded = 10, wickets = 1)
            )
        )
        val match2 = Match(id = "m2", teamA = "Team A", teamB = "Team C", status = MatchStatus.COMPLETED, innings1 = inn2)
        repository.createMatch(match2)

        val rankings = getPlayerRankingsUseCase().first()

        // Verify matches count
        assertEquals(2, rankings.totalMatchesAnalyzed)

        // Verify Batsmen:
        // Rohit Sharma total: 30 + 80 = 110 runs in 2 innings (Highest: 80)
        // Virat Kohli total: 70 + 50 = 120 runs in 2 innings (Highest: 70)
        // Since deduplication is strictly enforced, each player MUST appear EXACTLY once!
        assertEquals(2, rankings.batsmen.size)

        val rank1Batter = rankings.batsmen[0]
        assertEquals(1, rank1Batter.rank)
        assertEquals("Virat Kohli", rank1Batter.name)
        assertEquals(120, rank1Batter.totalRuns)
        assertEquals(70, rank1Batter.ballsFaced)
        assertEquals(2, rank1Batter.inningsCount)
        assertEquals(13, rank1Batter.fours)
        assertEquals(3, rank1Batter.sixes)

        val rank2Batter = rankings.batsmen[1]
        assertEquals(2, rank2Batter.rank)
        assertEquals("Rohit Sharma", rank2Batter.name)
        assertEquals(110, rank2Batter.totalRuns)
        assertEquals(65, rank2Batter.ballsFaced)
        assertEquals(2, rank2Batter.inningsCount)

        // Verify Bowler:
        // Jasprit Bumrah should appear EXACTLY once with total 3 wickets (2 + 1) and 22 runs conceded
        assertEquals(1, rankings.bowlers.size)
        val rank1Bowler = rankings.bowlers[0]
        assertEquals(1, rank1Bowler.rank)
        assertEquals("Jasprit Bumrah", rank1Bowler.name)
        assertEquals(3, rank1Bowler.wickets)
        assertEquals(22, rank1Bowler.runsConceded)
        assertEquals(24, rank1Bowler.totalBalls) // 2 overs + 2 overs = 24 balls
    }

    @Test
    fun testTieBreakerRankings() = runBlocking {
        // Two batters with equal runs: Batter A has higher highest score than Batter B
        val inn = Innings(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            batters = listOf(
                BatterScore(id = "b1", name = "Batter Alpha", runs = 50, ballsFaced = 30, fours = 5, sixes = 0),
                BatterScore(id = "b2", name = "Batter Beta", runs = 50, ballsFaced = 25, fours = 3, sixes = 2)
            ),
            bowlers = listOf(
                // Bowler Alpha: 2 wickets for 20 runs in 12 balls (Econ: 10.0)
                BowlerScore(id = "bw1", name = "Bowler Alpha", oversBowled = 2, ballsBowledInOver = 0, runsConceded = 20, wickets = 2),
                // Bowler Beta: 2 wickets for 12 runs in 12 balls (Econ: 6.0)
                BowlerScore(id = "bw2", name = "Bowler Beta", oversBowled = 2, ballsBowledInOver = 0, runsConceded = 12, wickets = 2)
            )
        )
        val match = Match(id = "m1", teamA = "Team A", teamB = "Team B", status = MatchStatus.COMPLETED, innings1 = inn)
        repository.createMatch(match)

        val rankings = getPlayerRankingsUseCase().first()

        // Equal wickets: Bowler Beta (economy 6.0) should rank #1 over Bowler Alpha (economy 10.0)
        assertEquals(2, rankings.bowlers.size)
        assertEquals("Bowler Beta", rankings.bowlers[0].name)
        assertEquals(1, rankings.bowlers[0].rank)
        assertEquals("Bowler Alpha", rankings.bowlers[1].name)
        assertEquals(2, rankings.bowlers[1].rank)
    }

    @Test
    fun testTestMatchesFilterAllPlayersExceptKashishAndExclude123vs456() = runBlocking {
        // Match 123 vs 456 (F2A7oeEcJkZ6ZS2z4aoF) - should be completely excluded
        val inn123 = Innings(
            inningsNumber = 1,
            battingTeam = "123",
            bowlingTeam = "456",
            batters = listOf(
                BatterScore(id = "b1", name = "virat", runs = 16, ballsFaced = 10, isOut = true),
                BatterScore(id = "b2", name = "dhoni", runs = 67, ballsFaced = 30, isOut = true)
            ),
            bowlers = listOf(
                BowlerScore(id = "bw1", name = "kashish", oversBowled = 1, ballsBowledInOver = 0, runsConceded = 10, wickets = 1)
            )
        )
        val match123 = Match(id = "F2A7oeEcJkZ6ZS2z4aoF", teamA = "123", teamB = "456", status = MatchStatus.COMPLETED, innings1 = inn123)
        repository.createMatch(match123)

        // Another test match with dummy players and Kashish
        val innTest = Innings(
            inningsNumber = 1,
            battingTeam = "hhn",
            bowlingTeam = "hhbn",
            batters = listOf(
                BatterScore(id = "b3", name = "Striker 1", runs = 10, ballsFaced = 5, isOut = true),
                BatterScore(id = "b4", name = "Ddhdf", runs = 40, ballsFaced = 20, isOut = true)
            ),
            bowlers = listOf(
                BowlerScore(id = "bw2", name = "Kashish", oversBowled = 2, ballsBowledInOver = 0, runsConceded = 8, wickets = 4),
                BowlerScore(id = "bw3", name = "Nishtha", oversBowled = 1, ballsBowledInOver = 0, runsConceded = 15, wickets = 2)
            )
        )
        val matchTest = Match(id = "BqSQumBqWYACyUNqUejC", teamA = "hhn", teamB = "hhbn", status = MatchStatus.COMPLETED, innings1 = innTest)
        repository.createMatch(matchTest)

        val rankings = getPlayerRankingsUseCase().first()

        // Match 123 vs 456 is excluded: virat and dhoni must NOT be present
        assertTrue(rankings.batsmen.none { it.name.contains("Virat", ignoreCase = true) })
        assertTrue(rankings.batsmen.none { it.name.contains("Dhoni", ignoreCase = true) })
        // Dummy players from test match must be excluded
        assertTrue(rankings.batsmen.none { it.name.contains("Striker", ignoreCase = true) })
        assertTrue(rankings.batsmen.none { it.name.contains("Ddhdf", ignoreCase = true) })

        // Bowlers: Only Kashish should remain (Nishtha must be excluded from test matches)
        assertEquals(1, rankings.bowlers.size)
        assertEquals("Kashish", rankings.bowlers[0].name)
        assertEquals(4, rankings.bowlers[0].wickets)
    }
}
