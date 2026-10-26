package com.example.igccxi

import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.domain.ScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoringEngineTest {

    private lateinit var match: Match

    @Before
    fun setUp() {
        val initialMatch = Match(
            id = "test_match_1",
            teamA = "India",
            teamB = "Australia",
            date = "2026-10-25",
            totalOvers = 2,
            tossWinner = "India",
            tossDecision = "bat"
        )
        match = ScoringEngine.startMatch(
            match = initialMatch,
            strikerName = "Rohit",
            nonStrikerName = "Kohli",
            bowlerName = "Starc"
        )
    }

    @Test
    fun testStartMatch_initializesCorrectly() {
        assertEquals(MatchStatus.INNINGS_1, match.status)
        assertEquals("India", match.currentInnings.battingTeam)
        assertEquals("Australia", match.currentInnings.bowlingTeam)
        assertEquals(0, match.currentInnings.totalRuns)
        assertEquals(0, match.currentInnings.wickets)
        assertEquals(0, match.currentInnings.oversBowled)
        assertEquals(0, match.currentInnings.ballsBowledInOver)

        val striker = match.currentInnings.getStriker()
        val nonStriker = match.currentInnings.getNonStriker()
        assertEquals("Rohit", striker?.name)
        assertEquals("Kohli", nonStriker?.name)
    }

    @Test
    fun testLegalRunsAndOddRunStrikeRotation() {
        // Rohit on strike, scores 1 run
        val m1 = ScoringEngine.recordRuns(match, 1)
        assertEquals(1, m1.currentInnings.totalRuns)
        assertEquals(1, m1.currentInnings.ballsBowledInOver)
        assertEquals(0, m1.currentInnings.oversBowled)

        // Rohit should have 1 run in 1 ball
        val rohit = m1.currentInnings.batters.find { it.name == "Rohit" }
        assertEquals(1, rohit?.runs)
        assertEquals(1, rohit?.ballsFaced)

        // Kohli should now be on strike because 1 is an odd run
        assertEquals("Kohli", m1.currentInnings.getStriker()?.name)
        assertEquals("Rohit", m1.currentInnings.getNonStriker()?.name)

        // Kohli scores 2 runs (even), strike should stay with Kohli
        val m2 = ScoringEngine.recordRuns(m1, 2)
        assertEquals(3, m2.currentInnings.totalRuns)
        assertEquals(2, m2.currentInnings.ballsBowledInOver)
        assertEquals("Kohli", m2.currentInnings.getStriker()?.name)

        // Kohli scores a 4
        val m3 = ScoringEngine.recordRuns(m2, 4)
        val kohli = m3.currentInnings.batters.find { it.name == "Kohli" }
        assertEquals(6, kohli?.runs)
        assertEquals(1, kohli?.fours)
        assertEquals(2, kohli?.ballsFaced)
    }

    @Test
    fun testWideDoesNotAdvanceLegalBalls() {
        // Bowler delivers a wide
        val m1 = ScoringEngine.recordWide(match, 1)

        assertEquals(1, m1.currentInnings.totalRuns)
        assertEquals(1, m1.currentInnings.extras.wides)
        assertEquals(0, m1.currentInnings.ballsBowledInOver) // Crucial: legal ball remains 0!
        assertEquals(0, m1.currentInnings.oversBowled)

        // Rohit balls faced should still be 0
        val rohit = m1.currentInnings.getStriker()
        assertEquals(0, rohit?.ballsFaced)

        // Bowler runs conceded should be 1
        val starc = m1.currentInnings.getCurrentBowler()
        assertEquals(1, starc?.runsConceded)
    }

    @Test
    fun testNoBallAddsPenaltyAndBatterRunsWithoutAdvancingLegalBalls() {
        // Rohit hits 4 runs off a No Ball
        val m1 = ScoringEngine.recordNoBall(match, runsOffBat = 4)

        // 1 (penalty) + 4 (off bat) = 5 total runs
        assertEquals(5, m1.currentInnings.totalRuns)
        assertEquals(1, m1.currentInnings.extras.noBalls)
        assertEquals(0, m1.currentInnings.ballsBowledInOver) // Legal ball does not advance

        val rohit = m1.currentInnings.batters.find { it.name == "Rohit" }
        assertEquals(4, rohit?.runs)
        assertEquals(1, rohit?.fours)
    }

    @Test
    fun testOverCompletionSwapsStrikeAndIncrementsOver() {
        var current = match
        // Bowl 5 dot balls
        for (i in 1..5) {
            current = ScoringEngine.recordRuns(current, 0)
        }
        assertEquals(0, current.currentInnings.oversBowled)
        assertEquals(5, current.currentInnings.ballsBowledInOver)
        assertEquals("Rohit", current.currentInnings.getStriker()?.name)

        // 6th ball: 0 runs. End of over!
        current = ScoringEngine.recordRuns(current, 0)
        assertEquals(1, current.currentInnings.oversBowled)
        assertEquals(0, current.currentInnings.ballsBowledInOver)

        // Strike swaps at end of over -> Kohli is now on strike!
        assertEquals("Kohli", current.currentInnings.getStriker()?.name)
        assertEquals("Rohit", current.currentInnings.getNonStriker()?.name)
    }

    @Test
    fun testWicketMarksBatterOutAndIntroducesNewBatter() {
        val m1 = ScoringEngine.recordWicket(
            match = match,
            dismissalDescription = "b Starc",
            newBatterName = "Suryakumar"
        )

        assertEquals(1, m1.currentInnings.wickets)
        assertEquals(1, m1.currentInnings.ballsBowledInOver)

        val rohit = m1.currentInnings.batters.find { it.name == "Rohit" }
        assertTrue(rohit?.isOut == true)
        assertEquals("b Starc", rohit?.dismissal)

        val currentStriker = m1.currentInnings.getStriker()
        assertEquals("Suryakumar", currentStriker?.name)
        assertFalse(currentStriker?.isOut == true)

        val bowler = m1.currentInnings.getCurrentBowler()
        assertEquals(1, bowler?.wickets)
    }

    @Test
    fun testEndInnings1SetsTarget() {
        var current = match
        // Score 10 runs in inning 1
        current = ScoringEngine.recordRuns(current, 6)
        current = ScoringEngine.recordRuns(current, 4)

        // End Innings 1
        val inningBreak = ScoringEngine.endInnings(current)
        assertEquals(MatchStatus.INNINGS_BREAK, inningBreak.status)
        assertEquals(11, inningBreak.target) // Target = 10 + 1
    }

    @Test
    fun testSecondInningsChasingTargetWinsByWickets() {
        var current = match
        current = ScoringEngine.recordRuns(current, 4) // 4 runs
        val breakMatch = ScoringEngine.endInnings(current) // Target is 5

        val inn2 = ScoringEngine.startSecondInnings(
            match = breakMatch,
            strikerName = "Warner",
            nonStrikerName = "Head",
            bowlerName = "Bumrah"
        )
        assertEquals(MatchStatus.INNINGS_2, inn2.status)
        assertEquals("Australia", inn2.currentInnings.battingTeam)

        // Australia hits a six -> 6 runs >= target 5!
        val wonMatch = ScoringEngine.recordRuns(inn2, 6)
        assertEquals(MatchStatus.COMPLETED, wonMatch.status)
        assertEquals("Australia won by 10 wickets", wonMatch.result)
    }

    @Test
    fun testSecondInningsDefendedWinsByRuns() {
        var current = match
        current = ScoringEngine.recordRuns(current, 6) // India scored 6
        val breakMatch = ScoringEngine.endInnings(current) // Target is 7

        val inn2 = ScoringEngine.startSecondInnings(
            match = breakMatch,
            strikerName = "Warner",
            nonStrikerName = "Head",
            bowlerName = "Bumrah"
        )

        // Australia scores 2 runs then innings is ended
        val scored2 = ScoringEngine.recordRuns(inn2, 2)
        val finalMatch = ScoringEngine.endInnings(scored2)

        assertEquals(MatchStatus.COMPLETED, finalMatch.status)
        assertEquals("India won by 4 runs", finalMatch.result)
    }

    @Test
    fun testStartMatch_teamNamesArePreservedAndNeverTruncated() {
        // Case 1: Team B won toss and elected to bat
        val matchTeamBBat = ScoringEngine.startMatch(
            match = Match(
                teamA = "Royal Challengers",
                teamB = "Super Kings",
                tossWinner = "Super Kings",
                tossDecision = "bat",
                totalOvers = 20
            )
        )
        assertEquals("Super Kings", matchTeamBBat.currentInnings.battingTeam)
        assertEquals("Royal Challengers", matchTeamBBat.currentInnings.bowlingTeam)

        // Case 2: Team B won toss and elected to bowl
        val matchTeamBBowl = ScoringEngine.startMatch(
            match = Match(
                teamA = "Royal Challengers",
                teamB = "Super Kings",
                tossWinner = "Super Kings",
                tossDecision = "bowl",
                totalOvers = 20
            )
        )
        assertEquals("Royal Challengers", matchTeamBBowl.currentInnings.battingTeam)
        assertEquals("Super Kings", matchTeamBBowl.currentInnings.bowlingTeam)

        // Case 3: Imperfect single-char tossWinner fallback
        val matchFallback = ScoringEngine.startMatch(
            match = Match(
                teamA = "Royal Challengers",
                teamB = "Super Kings",
                tossWinner = "R",
                tossDecision = "bat",
                totalOvers = 20
            )
        )
        assertEquals("Royal Challengers", matchFallback.currentInnings.battingTeam)
        assertEquals("Super Kings", matchFallback.currentInnings.bowlingTeam)
    }

    @Test
    fun testMaidenOverCalculation_incrementsOnZeroRunsConceded() {
        var current = match
        repeat(6) {
            current = ScoringEngine.recordRuns(current, 0)
        }
        val bowler = current.currentInnings.bowlers.find { it.name == "Starc" }
        assertEquals(1, bowler?.oversBowled)
        assertEquals(1, bowler?.maidens)
        assertEquals(0, bowler?.runsConceded)
    }

    @Test
    fun testMaidenOver_notRecordedWhenRunsConceded() {
        var current = match
        repeat(5) {
            current = ScoringEngine.recordRuns(current, 0)
        }
        current = ScoringEngine.recordRuns(current, 1) // 1 run conceded on 6th ball
        val bowler = current.currentInnings.bowlers.find { it.name == "Starc" }
        assertEquals(1, bowler?.oversBowled)
        assertEquals(0, bowler?.maidens)
        assertEquals(1, bowler?.runsConceded)
    }

    @Test
    fun testRunOut_doesNotCreditBowlerWithWicket() {
        var current = match
        // Striker run out
        current = ScoringEngine.recordWicket(
            match = current,
            dismissalDescription = "run out (Kohli)",
            newBatterName = "Pant",
            isStrikerOut = true
        )
        assertEquals(1, current.currentInnings.wickets)
        val bowler = current.currentInnings.bowlers.find { it.name == "Starc" }
        // Bowler should NOT receive credit for a run out (MCC Law 38)
        assertEquals(0, bowler?.wickets)
        assertEquals("Pant", current.currentInnings.getStriker()?.name)
        assertEquals("Kohli", current.currentInnings.getNonStriker()?.name)
    }

    @Test
    fun testRunOutNonStriker_dismissesNonStrikerLeavesStriker() {
        var current = match
        // Non-striker (Kohli) run out
        current = ScoringEngine.recordWicket(
            match = current,
            dismissalDescription = "run out (Direct Hit)",
            newBatterName = "Pant",
            isStrikerOut = false
        )
        assertEquals(1, current.currentInnings.wickets)
        // Striker (Rohit) remains on strike
        assertEquals("Rohit", current.currentInnings.getStriker()?.name)
        // Non-striker is now Pant
        assertEquals("Pant", current.currentInnings.getNonStriker()?.name)
        val kohli = current.currentInnings.batters.find { it.name == "Kohli" }
        assertTrue(kohli?.isOut == true)
        val bowler = current.currentInnings.bowlers.find { it.name == "Starc" }
        assertEquals(0, bowler?.wickets)
    }

    @Test
    fun testByesAndLegByes_accrueToTeamAndExtrasNotBatsmanOrBowler() {
        var current = match
        // Rohit faces ball 1, runs 1 Bye
        current = ScoringEngine.recordBye(current, 1)
        assertEquals(1, current.currentInnings.totalRuns)
        assertEquals(1, current.currentInnings.extras.byes)
        assertEquals(1, current.currentInnings.ballsBowledInOver)

        // Rohit ballsFaced increments, but personal runs stay 0
        val rohit = current.currentInnings.batters.find { it.name == "Rohit" }
        assertEquals(0, rohit?.runs)
        assertEquals(1, rohit?.ballsFaced)

        // Bowler runsConceded stays 0
        val bowler = current.currentInnings.bowlers.find { it.name == "Starc" }
        assertEquals(0, bowler?.runsConceded)

        // Odd runs swap strike -> Kohli is now striker
        assertEquals("Kohli", current.currentInnings.getStriker()?.name)

        // Kohli runs 2 Leg Byes (even -> strike stays with Kohli)
        current = ScoringEngine.recordLegBye(current, 2)
        assertEquals(3, current.currentInnings.totalRuns)
        assertEquals(2, current.currentInnings.extras.legByes)
        assertEquals(2, current.currentInnings.ballsBowledInOver)
        assertEquals("Kohli", current.currentInnings.getStriker()?.name)
    }

    @Test
    fun testFinalResultGrammar_singularRunAndWicket() {
        // Target is 5, Team B scores 5 -> 10 - 9 wickets = 1 wicket win
        val m1 = Match(
            totalOvers = 5,
            teamA = "India",
            teamB = "Australia",
            currentInningsNumber = 2,
            target = 5,
            innings1 = Innings(totalRuns = 4, isCompleted = true),
            innings2 = Innings(battingTeam = "Australia", totalRuns = 5, wickets = 9)
        )
        val resWicket = ScoringEngine.calculateFinalResult(m1)
        assertEquals("Australia won by 1 wicket", resWicket)

        // Team A defended by 1 run
        val m2 = Match(
            totalOvers = 5,
            teamA = "India",
            teamB = "Australia",
            currentInningsNumber = 2,
            target = 10,
            innings1 = Innings(battingTeam = "India", totalRuns = 9, isCompleted = true),
            innings2 = Innings(battingTeam = "Australia", totalRuns = 8, isCompleted = true)
        )
        val resRun = ScoringEngine.calculateFinalResult(m2)
        assertEquals("India won by 1 run", resRun)
    }
}
