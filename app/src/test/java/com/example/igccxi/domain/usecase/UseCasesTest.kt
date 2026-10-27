package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import com.example.igccxi.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FakeMatchRepository : MatchRepository {
    private val matches = MutableStateFlow<List<Match>>(emptyList())

    override fun observeMatches(): Flow<List<Match>> = matches

    override fun observeMatch(matchId: String): Flow<Match?> {
        return matches.map { list -> list.find { it.id == matchId } }
    }

    override suspend fun createMatch(match: Match): String {
        val id = if (match.id.isNotEmpty()) match.id else "gen_id"
        matches.value = listOf(match.copy(id = id)) + matches.value
        return id
    }

    override suspend fun updateMatch(match: Match) {
        matches.value = matches.value.map { if (it.id == match.id) match else it }
    }

    override suspend fun deleteMatch(matchId: String) {
        matches.value = matches.value.filterNot { it.id == matchId }
    }

    override suspend fun getCurrentUserId(): String = "test_user_123"
}

class UseCasesTest {

    private lateinit var repository: FakeMatchRepository
    private lateinit var getMatchesUseCase: GetMatchesUseCase
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var createMatchUseCase: CreateMatchUseCase
    private lateinit var updateMatchUseCase: UpdateMatchUseCase
    private lateinit var deleteMatchUseCase: DeleteMatchUseCase
    private lateinit var getCurrentUserIdUseCase: GetCurrentUserIdUseCase

    @Before
    fun setUp() {
        repository = FakeMatchRepository()
        getMatchesUseCase = GetMatchesUseCase(repository)
        getMatchByIdUseCase = GetMatchByIdUseCase(repository)
        createMatchUseCase = CreateMatchUseCase(repository)
        updateMatchUseCase = UpdateMatchUseCase(repository)
        deleteMatchUseCase = DeleteMatchUseCase(repository)
        getCurrentUserIdUseCase = GetCurrentUserIdUseCase(repository)
    }

    @Test
    fun testCreateAndGetMatches() = runBlocking {
        val match = Match(id = "m1", teamA = "Team 1", teamB = "Team 2", status = MatchStatus.INNINGS_1)
        val createdId = createMatchUseCase(match)

        assertEquals("m1", createdId)
        val allMatches = getMatchesUseCase().first()
        assertEquals(1, allMatches.size)
        assertEquals("Team 1", allMatches.first().teamA)

        val fetchedMatch = getMatchByIdUseCase("m1").first()
        assertNotNull(fetchedMatch)
        assertEquals("Team 2", fetchedMatch?.teamB)
    }

    @Test
    fun testUpdateMatch() = runBlocking {
        val match = Match(id = "m1", teamA = "Team 1", teamB = "Team 2", status = MatchStatus.INNINGS_1)
        createMatchUseCase(match)

        val updated = match.copy(status = MatchStatus.COMPLETED, result = "Team 1 won")
        updateMatchUseCase(updated)

        val fetched = getMatchByIdUseCase("m1").first()
        assertEquals(MatchStatus.COMPLETED, fetched?.status)
        assertEquals("Team 1 won", fetched?.result)
    }

    @Test
    fun testDeleteMatch() = runBlocking {
        val match = Match(id = "m1", teamA = "Team 1", teamB = "Team 2", status = MatchStatus.INNINGS_1)
        createMatchUseCase(match)

        var allMatches = getMatchesUseCase().first()
        assertEquals(1, allMatches.size)

        deleteMatchUseCase("m1")

        allMatches = getMatchesUseCase().first()
        assertEquals(0, allMatches.size)
    }

    @Test
    fun testGetCurrentUserId() = runBlocking {
        val userId = getCurrentUserIdUseCase()
        assertEquals("test_user_123", userId)
    }
}
