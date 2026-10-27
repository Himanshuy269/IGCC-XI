package com.example.igccxi.domain

import com.example.igccxi.domain.model.BallEvent
import com.example.igccxi.domain.model.BallType
import com.example.igccxi.domain.model.BatterScore
import com.example.igccxi.domain.model.BowlerScore
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import java.util.UUID

object ScoringEngine {

    /**
     * Initializes match with the first innings, opening batsmen, and opening bowler.
     */
    fun startMatch(
        match: Match,
        strikerName: String = "Striker 1",
        nonStrikerName: String = "Non-Striker 2",
        bowlerName: String = "Bowler 1"
    ): Match {
        val cleanTeamA = match.teamA.trim().ifEmpty { "Team 1" }
        val cleanTeamB = match.teamB.trim().ifEmpty { "Team 2" }
        val tossWinnerIsTeamB = match.tossWinner.trim().equals(cleanTeamB, ignoreCase = true)
        val tossWinnerName = if (tossWinnerIsTeamB) cleanTeamB else cleanTeamA

        val battingTeam = if (match.tossDecision.equals("bat", ignoreCase = true)) {
            tossWinnerName
        } else {
            if (tossWinnerIsTeamB) cleanTeamA else cleanTeamB
        }
        val bowlingTeam = if (battingTeam.equals(cleanTeamA, ignoreCase = true)) cleanTeamB else cleanTeamA

        val striker = BatterScore(id = "b1", name = strikerName)
        val nonStriker = if (match.playersPerTeam > 1) BatterScore(id = "b2", name = nonStrikerName) else null
        val bowler = BowlerScore(id = "bw1", name = bowlerName)

        val innings1 = Innings(
            inningsNumber = 1,
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            currentStrikerId = striker.id,
            currentNonStrikerId = nonStriker?.id ?: "",
            currentBowlerId = bowler.id,
            batters = if (nonStriker != null) listOf(striker, nonStriker) else listOf(striker),
            bowlers = listOf(bowler)
        )

        return match.copy(
            teamA = cleanTeamA,
            teamB = cleanTeamB,
            tossWinner = tossWinnerName,
            status = MatchStatus.INNINGS_1,
            currentInningsNumber = 1,
            innings1 = innings1
        )
    }

    /**
     * Records runs scored off a legal delivery (0, 1, 2, 3, 4, 6).
     */
    fun recordRuns(match: Match, runs: Int): Match {
        if (!match.isScorable) return match
        val current = match.currentInnings
        val striker = current.getStriker() ?: return match
        val bowler = current.getCurrentBowler() ?: return match

        // 1. Update striker stats
        val updatedStriker = striker.copy(
            runs = striker.runs + runs,
            ballsFaced = striker.ballsFaced + 1,
            fours = if (runs == 4) striker.fours + 1 else striker.fours,
            sixes = if (runs == 6) striker.sixes + 1 else striker.sixes
        )

        // 2. Update bowler stats
        val nextBallsInOver = current.ballsBowledInOver + 1
        val isOverComplete = nextBallsInOver >= 6

        var updatedBowler = bowler.copy(
            runsConceded = bowler.runsConceded + runs,
            oversBowled = if (isOverComplete) bowler.oversBowled + 1 else bowler.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver
        )

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.NORMAL,
            runsOffBat = runs,
            extraRuns = 0,
            isLegalBall = true,
            isWicket = false,
            batterId = striker.id,
            bowlerId = bowler.id,
            displayText = runs.toString()
        )

        if (isOverComplete) {
            updatedBowler = checkAndApplyMaiden(updatedBowler, current.recentBalls, ballEvent)
        }

        // 3. Batters & Bowlers list updates
        val updatedBatters = current.batters.map { if (it.id == updatedStriker.id) updatedStriker else it }
        val updatedBowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it }

        // 4. Strike rotation: odd runs swap strike (only if non-striker exists)
        var newStrikerId = current.currentStrikerId
        var newNonStrikerId = current.currentNonStrikerId
        val hasNonStriker = newNonStrikerId.isNotBlank() && current.batters.any { it.id == newNonStrikerId && !it.isOut }
        if (runs % 2 != 0 && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }

        // End of over: swap strike again (only if non-striker exists)
        if (isOverComplete && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }

        val updatedInnings = current.copy(
            totalRuns = current.totalRuns + runs,
            oversBowled = if (isOverComplete) current.oversBowled + 1 else current.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver,
            currentStrikerId = newStrikerId,
            currentNonStrikerId = newNonStrikerId,
            batters = updatedBatters,
            bowlers = updatedBowlers,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Records a Wide delivery (+1 extra run, bowler conceded +1, legal ball count not incremented).
     */
    fun recordWide(match: Match, extraRuns: Int = 1): Match {
        if (!match.isScorable) return match
        val current = match.currentInnings
        val bowler = current.getCurrentBowler() ?: return match

        val updatedBowler = bowler.copy(
            runsConceded = bowler.runsConceded + extraRuns
        )

        val updatedBowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it }
        val updatedExtras = current.extras.copy(wides = current.extras.wides + extraRuns)

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.WIDE,
            runsOffBat = 0,
            extraRuns = extraRuns,
            isLegalBall = false,
            isWicket = false,
            batterId = current.currentStrikerId,
            bowlerId = bowler.id,
            displayText = if (extraRuns > 1) "${extraRuns}Wd" else "Wd"
        )

        val updatedInnings = current.copy(
            totalRuns = current.totalRuns + extraRuns,
            extras = updatedExtras,
            bowlers = updatedBowlers,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Records a No Ball delivery (+1 extra run + any runs scored off bat, legal ball count not incremented).
     */
    fun recordNoBall(match: Match, runsOffBat: Int = 0): Match {
        if (!match.isScorable) return match
        val current = match.currentInnings
        val striker = current.getStriker() ?: return match
        val bowler = current.getCurrentBowler() ?: return match

        val totalRunAdded = 1 + runsOffBat

        val updatedStriker = striker.copy(
            runs = striker.runs + runsOffBat,
            ballsFaced = striker.ballsFaced + 1,
            fours = if (runsOffBat == 4) striker.fours + 1 else striker.fours,
            sixes = if (runsOffBat == 6) striker.sixes + 1 else striker.sixes
        )

        val updatedBowler = bowler.copy(
            runsConceded = bowler.runsConceded + totalRunAdded
        )

        val updatedBatters = current.batters.map { if (it.id == updatedStriker.id) updatedStriker else it }
        val updatedBowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it }
        val updatedExtras = current.extras.copy(noBalls = current.extras.noBalls + 1)

        // Strike rotation on odd runs off bat (only if non-striker exists)
        var newStrikerId = current.currentStrikerId
        var newNonStrikerId = current.currentNonStrikerId
        val hasNonStriker = newNonStrikerId.isNotBlank() && current.batters.any { it.id == newNonStrikerId && !it.isOut }
        if (runsOffBat % 2 != 0 && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.NO_BALL,
            runsOffBat = runsOffBat,
            extraRuns = 1,
            isLegalBall = false,
            isWicket = false,
            batterId = striker.id,
            bowlerId = bowler.id,
            displayText = if (runsOffBat > 0) "Nb+$runsOffBat" else "Nb"
        )

        val updatedInnings = current.copy(
            totalRuns = current.totalRuns + totalRunAdded,
            extras = updatedExtras,
            batters = updatedBatters,
            bowlers = updatedBowlers,
            currentStrikerId = newStrikerId,
            currentNonStrikerId = newNonStrikerId,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Records a Wicket delivery. Legal ball, increments wickets, marks batter out.
     * Enforces MCC Law 33-40: Run outs and retirements do not credit the bowler with a wicket.
     * Supports dismissing either the striker or the non-striker (e.g. run out at non-striker end).
     */
    fun recordWicket(
        match: Match,
        dismissalDescription: String = "bowled",
        newBatterName: String? = null,
        isStrikerOut: Boolean = true
    ): Match {
        if (!match.isScorable) return match
        val current = match.currentInnings
        val striker = current.getStriker() ?: return match
        val nonStriker = current.getNonStriker()
        val bowler = current.getCurrentBowler() ?: return match
        val isLoneBatterMode = nonStriker == null || current.currentNonStrikerId.isBlank()

        // In cricket rules (MCC Law 33-40):
        // Run outs, retired hurt/out, timed out, and obstructing the field are NOT credited to the bowler.
        val isBowlerWicket = !dismissalDescription.contains("run out", ignoreCase = true) &&
                !dismissalDescription.contains("retired", ignoreCase = true) &&
                !dismissalDescription.contains("timed out", ignoreCase = true) &&
                !dismissalDescription.contains("obstructing", ignoreCase = true)

        val nextBallsInOver = current.ballsBowledInOver + 1
        val isOverComplete = nextBallsInOver >= 6

        var updatedBowler = bowler.copy(
            wickets = if (isBowlerWicket) bowler.wickets + 1 else bowler.wickets,
            oversBowled = if (isOverComplete) bowler.oversBowled + 1 else bowler.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver
        )

        // When batting alone, the lone striker is the only one who can be out
        val effectiveStrikerOut = if (isLoneBatterMode) true else isStrikerOut

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.WICKET,
            runsOffBat = 0,
            extraRuns = 0,
            isLegalBall = true,
            isWicket = true,
            batterId = if (effectiveStrikerOut) striker.id else (nonStriker?.id ?: striker.id),
            bowlerId = bowler.id,
            displayText = if (isBowlerWicket) "W" else "W(RO)"
        )

        if (isOverComplete) {
            updatedBowler = checkAndApplyMaiden(updatedBowler, current.recentBalls, ballEvent)
        }

        // Striker always faced this legal delivery
        val updatedStriker = if (effectiveStrikerOut) {
            striker.copy(
                ballsFaced = striker.ballsFaced + 1,
                isOut = true,
                dismissal = dismissalDescription
            )
        } else {
            striker.copy(ballsFaced = striker.ballsFaced + 1)
        }

        val updatedNonStriker = if (!isLoneBatterMode && !effectiveStrikerOut) {
            nonStriker?.copy(
                isOut = true,
                dismissal = dismissalDescription
            )
        } else {
            nonStriker
        }

        val nextWickets = current.wickets + 1
        val isAllOut = nextWickets >= match.maxWickets

        var battersList = current.batters.map {
            when (it.id) {
                updatedStriker.id -> updatedStriker
                updatedNonStriker?.id -> updatedNonStriker
                else -> it
            }
        }

        var nextStrikerId = updatedStriker.id
        var nextNonStrikerId = updatedNonStriker?.id ?: ""

        if (!isAllOut) {
            // Under Gully Cricket rules: team of X players gets full X wickets.
            // When (X-1) wickets fall, all X players have batted, so no new batter enters.
            // The surviving batter stays and continues batting ALONE.
            val isNowLoneBatsman = nextWickets >= match.maxWickets - 1

            if (isNowLoneBatsman) {
                val survivingBatter = if (effectiveStrikerOut) updatedNonStriker else updatedStriker
                nextStrikerId = survivingBatter?.id ?: updatedStriker.id
                nextNonStrikerId = ""
            } else {
                val candidate = newBatterName?.trim()
                val batterName = if (!candidate.isNullOrEmpty()) {
                    candidate
                } else {
                    var nextNum = battersList.size + 1
                    while (battersList.any { it.name.trim().equals("Batter $nextNum", ignoreCase = true) }) {
                        nextNum++
                    }
                    "Batter $nextNum"
                }
                val newBatter = BatterScore(
                    id = "b${battersList.size + 1}_${System.currentTimeMillis() % 1000}",
                    name = batterName
                )
                battersList = battersList + newBatter

                if (effectiveStrikerOut) {
                    nextStrikerId = newBatter.id
                } else {
                    nextNonStrikerId = newBatter.id
                }
            }
        } else {
            nextStrikerId = ""
            nextNonStrikerId = ""
        }

        if (isOverComplete && !isAllOut && nextNonStrikerId.isNotBlank()) {
            val temp = nextStrikerId
            nextStrikerId = nextNonStrikerId
            nextNonStrikerId = temp
        }

        val updatedInnings = current.copy(
            wickets = nextWickets,
            oversBowled = if (isOverComplete) current.oversBowled + 1 else current.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver,
            batters = battersList,
            bowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it },
            currentStrikerId = nextStrikerId,
            currentNonStrikerId = nextNonStrikerId,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Records Byes (MCC Law 23: legal delivery, runs added to team & extras.byes,
     * batsman faced ball but no runs off bat, bowler not charged).
     */
    fun recordBye(match: Match, runs: Int = 1): Match {
        if (!match.isScorable || runs < 1) return match
        val current = match.currentInnings
        val striker = current.getStriker() ?: return match
        val bowler = current.getCurrentBowler() ?: return match

        val nextBallsInOver = current.ballsBowledInOver + 1
        val isOverComplete = nextBallsInOver >= 6

        val updatedStriker = striker.copy(
            ballsFaced = striker.ballsFaced + 1
        )

        var updatedBowler = bowler.copy(
            oversBowled = if (isOverComplete) bowler.oversBowled + 1 else bowler.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver
        )

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.BYE,
            runsOffBat = 0,
            extraRuns = runs,
            isLegalBall = true,
            isWicket = false,
            batterId = striker.id,
            bowlerId = bowler.id,
            displayText = if (runs > 1) "${runs}B" else "B"
        )

        if (isOverComplete) {
            updatedBowler = checkAndApplyMaiden(updatedBowler, current.recentBalls, ballEvent)
        }

        val updatedBatters = current.batters.map { if (it.id == updatedStriker.id) updatedStriker else it }
        val updatedBowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it }
        val updatedExtras = current.extras.copy(byes = current.extras.byes + runs)

        // Strike rotation on odd runs (only if non-striker exists)
        var newStrikerId = current.currentStrikerId
        var newNonStrikerId = current.currentNonStrikerId
        val hasNonStriker = newNonStrikerId.isNotBlank() && current.batters.any { it.id == newNonStrikerId && !it.isOut }
        if (runs % 2 != 0 && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }
        if (isOverComplete && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }

        val updatedInnings = current.copy(
            totalRuns = current.totalRuns + runs,
            oversBowled = if (isOverComplete) current.oversBowled + 1 else current.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver,
            extras = updatedExtras,
            batters = updatedBatters,
            bowlers = updatedBowlers,
            currentStrikerId = newStrikerId,
            currentNonStrikerId = newNonStrikerId,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Records Leg Byes (MCC Law 24: legal delivery, runs added to team & extras.legByes,
     * batsman faced ball but no runs off bat, bowler not charged).
     */
    fun recordLegBye(match: Match, runs: Int = 1): Match {
        if (!match.isScorable || runs < 1) return match
        val current = match.currentInnings
        val striker = current.getStriker() ?: return match
        val bowler = current.getCurrentBowler() ?: return match

        val nextBallsInOver = current.ballsBowledInOver + 1
        val isOverComplete = nextBallsInOver >= 6

        val updatedStriker = striker.copy(
            ballsFaced = striker.ballsFaced + 1
        )

        var updatedBowler = bowler.copy(
            oversBowled = if (isOverComplete) bowler.oversBowled + 1 else bowler.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver
        )

        val ballEvent = BallEvent(
            id = UUID.randomUUID().toString(),
            type = BallType.LEG_BYE,
            runsOffBat = 0,
            extraRuns = runs,
            isLegalBall = true,
            isWicket = false,
            batterId = striker.id,
            bowlerId = bowler.id,
            displayText = if (runs > 1) "${runs}Lb" else "Lb"
        )

        if (isOverComplete) {
            updatedBowler = checkAndApplyMaiden(updatedBowler, current.recentBalls, ballEvent)
        }

        val updatedBatters = current.batters.map { if (it.id == updatedStriker.id) updatedStriker else it }
        val updatedBowlers = current.bowlers.map { if (it.id == updatedBowler.id) updatedBowler else it }
        val updatedExtras = current.extras.copy(legByes = current.extras.legByes + runs)

        // Strike rotation on odd runs (only if non-striker exists)
        var newStrikerId = current.currentStrikerId
        var newNonStrikerId = current.currentNonStrikerId
        val hasNonStriker = newNonStrikerId.isNotBlank() && current.batters.any { it.id == newNonStrikerId && !it.isOut }
        if (runs % 2 != 0 && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }
        if (isOverComplete && hasNonStriker) {
            val temp = newStrikerId
            newStrikerId = newNonStrikerId
            newNonStrikerId = temp
        }

        val updatedInnings = current.copy(
            totalRuns = current.totalRuns + runs,
            oversBowled = if (isOverComplete) current.oversBowled + 1 else current.oversBowled,
            ballsBowledInOver = if (isOverComplete) 0 else nextBallsInOver,
            extras = updatedExtras,
            batters = updatedBatters,
            bowlers = updatedBowlers,
            currentStrikerId = newStrikerId,
            currentNonStrikerId = newNonStrikerId,
            recentBalls = current.recentBalls + ballEvent
        )

        return finalizeBall(match, updatedInnings)
    }

    /**
     * Checks if a completed over was a maiden over (0 runs conceded off the bowler).
     * Byes and leg-byes are fielding extras and do not prevent a maiden.
     */
    private fun checkAndApplyMaiden(
        bowler: BowlerScore,
        currentRecentBalls: List<BallEvent>,
        lastBallEvent: BallEvent
    ): BowlerScore {
        val ballsInOver = mutableListOf<BallEvent>()
        ballsInOver.add(lastBallEvent)
        var legalCount = if (lastBallEvent.isLegalBall) 1 else 0

        for (i in currentRecentBalls.indices.reversed()) {
            if (legalCount >= 6) break
            val b = currentRecentBalls[i]
            if (b.bowlerId == bowler.id) {
                ballsInOver.add(b)
                if (b.isLegalBall) {
                    legalCount++
                }
            }
        }

        val runsConcededInOver = ballsInOver.sumOf { ball ->
            val bowlerExtras = if (ball.type == BallType.WIDE || ball.type == BallType.NO_BALL) ball.extraRuns else 0
            ball.runsOffBat + bowlerExtras
        }

        return if (runsConcededInOver == 0) {
            bowler.copy(maidens = bowler.maidens + 1)
        } else {
            bowler
        }
    }

    /**
     * Changes or introduces a bowler.
     */
    fun selectBowler(match: Match, bowlerName: String): Match {
        val current = match.currentInnings
        val trimmed = bowlerName.trim()
        if (trimmed.isEmpty()) return match

        val existingBowler = current.bowlers.find { it.name.equals(trimmed, ignoreCase = true) }
        val updatedBowlers: List<BowlerScore>
        val bowlerId: String

        if (existingBowler != null) {
            bowlerId = existingBowler.id
            updatedBowlers = current.bowlers
        } else {
            val newBowler = BowlerScore(
                id = "bw${current.bowlers.size + 1}_${System.currentTimeMillis() % 1000}",
                name = trimmed
            )
            bowlerId = newBowler.id
            updatedBowlers = current.bowlers + newBowler
        }

        val updatedInnings = current.copy(
            currentBowlerId = bowlerId,
            bowlers = updatedBowlers
        )

        return if (match.currentInningsNumber == 2) {
            match.copy(innings2 = updatedInnings)
        } else {
            match.copy(innings1 = updatedInnings)
        }
    }

    /**
     * Swaps strike between striker and non-striker.
     */
    fun swapStrike(match: Match): Match {
        val current = match.currentInnings
        if (current.currentNonStrikerId.isBlank() || current.getNonStriker() == null) {
            return match // Cannot swap strike when batting alone
        }
        val updatedInnings = current.copy(
            currentStrikerId = current.currentNonStrikerId,
            currentNonStrikerId = current.currentStrikerId
        )
        return if (match.currentInningsNumber == 2) {
            match.copy(innings2 = updatedInnings)
        } else {
            match.copy(innings1 = updatedInnings)
        }
    }

    /**
     * Ends the current innings manually or automatically.
     */
    fun endInnings(match: Match): Match {
        if (match.currentInningsNumber == 1) {
            val completedInnings1 = match.innings1.copy(isCompleted = true)
            val target = completedInnings1.totalRuns + 1
            return match.copy(
                status = MatchStatus.INNINGS_BREAK,
                innings1 = completedInnings1,
                target = target
            )
        } else {
            val completedInnings2 = match.innings2?.copy(isCompleted = true) ?: match.innings1
            val result = calculateFinalResult(match.copy(innings2 = completedInnings2))
            return match.copy(
                status = MatchStatus.COMPLETED,
                innings2 = completedInnings2,
                result = result
            )
        }
    }

    /**
     * Starts the 2nd innings with opening batters and opening bowler.
     */
    fun startSecondInnings(
        match: Match,
        strikerName: String = "Chasing Striker",
        nonStrikerName: String = "Chasing Non-Striker",
        bowlerName: String = "Opening Bowler"
    ): Match {
        val cleanTeamA = match.teamA.trim().ifEmpty { "Team 1" }
        val cleanTeamB = match.teamB.trim().ifEmpty { "Team 2" }
        val prevBatting = match.innings1.battingTeam.trim()
        val battingTeam = if (prevBatting.equals(cleanTeamA, ignoreCase = true)) cleanTeamB else cleanTeamA
        val bowlingTeam = if (battingTeam.equals(cleanTeamA, ignoreCase = true)) cleanTeamB else cleanTeamA

        val s = strikerName.trim().ifEmpty { "Chasing Striker 1" }
        var ns = nonStrikerName.trim().ifEmpty { "Chasing Striker 2" }
        if (s.equals(ns, ignoreCase = true)) {
            ns = "$ns (2)"
        }
        val b = bowlerName.trim().ifEmpty { "Opening Bowler" }

        val striker = BatterScore(id = "b2_1", name = s)
        val nonStriker = if (match.playersPerTeam > 1) BatterScore(id = "b2_2", name = ns) else null
        val bowler = BowlerScore(id = "bw2_1", name = b)

        val innings2 = Innings(
            inningsNumber = 2,
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            currentStrikerId = striker.id,
            currentNonStrikerId = nonStriker?.id ?: "",
            currentBowlerId = bowler.id,
            batters = if (nonStriker != null) listOf(striker, nonStriker) else listOf(striker),
            bowlers = listOf(bowler)
        )

        return match.copy(
            status = MatchStatus.INNINGS_2,
            currentInningsNumber = 2,
            innings2 = innings2
        )
    }

    /**
     * Finalizes the state of a match after any ball event:
     * - Checks for target achieved in 2nd innings
     * - Checks for overs completed
     * - Checks for all out
     */
    private fun finalizeBall(match: Match, updatedInnings: Innings): Match {
        val target = match.target
        val isSecondInnings = match.currentInningsNumber == 2

        if (isSecondInnings && target != null) {
            // Check if chasing team chased down target
            if (updatedInnings.totalRuns >= target) {
                val wicketsRemaining = match.maxWickets - updatedInnings.wickets
                val wText = if (wicketsRemaining == 1) "1 wicket" else "$wicketsRemaining wickets"
                val result = "${updatedInnings.battingTeam} won by $wText"
                return match.copy(
                    innings2 = updatedInnings.copy(isCompleted = true),
                    status = MatchStatus.COMPLETED,
                    result = result
                )
            }
        }

        // Check if overs completed or all out
        val isOversFinished = updatedInnings.oversBowled >= match.totalOvers
        val isAllOut = updatedInnings.wickets >= match.maxWickets

        if (isOversFinished || isAllOut) {
            if (!isSecondInnings) {
                // End of Innings 1
                val targetRun = updatedInnings.totalRuns + 1
                return match.copy(
                    innings1 = updatedInnings.copy(isCompleted = true),
                    status = MatchStatus.INNINGS_BREAK,
                    target = targetRun
                )
            } else {
                // End of Innings 2 -> Match Finished!
                val completedMatch = match.copy(innings2 = updatedInnings.copy(isCompleted = true))
                val result = calculateFinalResult(completedMatch)
                return completedMatch.copy(
                    status = MatchStatus.COMPLETED,
                    result = result
                )
            }
        }

        return if (isSecondInnings) {
            match.copy(innings2 = updatedInnings)
        } else {
            match.copy(innings1 = updatedInnings)
        }
    }

    /**
     * Determines match result when 2nd innings ends (all out or overs done).
     */
    fun calculateFinalResult(match: Match): String {
        val inn1 = match.innings1
        val inn2 = match.innings2 ?: return "Match in progress"

        return when {
            inn2.totalRuns > inn1.totalRuns -> {
                val wicketsLeft = match.maxWickets - inn2.wickets
                val wText = if (wicketsLeft == 1) "1 wicket" else "$wicketsLeft wickets"
                "${inn2.battingTeam} won by $wText"
            }
            inn1.totalRuns > inn2.totalRuns -> {
                val runsDiff = inn1.totalRuns - inn2.totalRuns
                val rText = if (runsDiff == 1) "1 run" else "$runsDiff runs"
                "${inn1.battingTeam} won by $rText"
            }
            else -> "Match tied"
        }
    }
}
