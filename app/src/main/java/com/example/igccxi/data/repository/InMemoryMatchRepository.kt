package com.example.igccxi.data.repository

import android.content.Context
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.repository.MatchRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class InMemoryMatchRepository(
    private val context: Context? = null,
    private val localUserId: String = "scorer_" + UUID.randomUUID().toString().take(6)
) : MatchRepository {

    private val gson = Gson()
    private val cacheFile: File? = context?.filesDir?.let { File(it, "matches_local_cache.json") }
    private val _matchesFlow = MutableStateFlow<List<Match>>(loadPersistedMatches())

    private fun loadPersistedMatches(): List<Match> {
        val file = cacheFile ?: return emptyList()
        return try {
            if (file.exists() && file.length() > 0L) {
                val json = file.readText()
                val type = object : TypeToken<List<Match>>() {}.type
                gson.fromJson<List<Match>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun persistMatches(matches: List<Match>) {
        val file = cacheFile ?: return
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(matches)
                file.writeText(json)
            } catch (_: Exception) {
                // Ignore disk write errors
            }
        }
    }

    override fun observeMatches(): Flow<List<Match>> = _matchesFlow

    override fun observeMatch(matchId: String): Flow<Match?> {
        return _matchesFlow.map { list -> list.find { it.id == matchId } }
    }

    override suspend fun createMatch(match: Match): String {
        val id = if (match.id.isNotEmpty()) match.id else "m_" + UUID.randomUUID().toString().take(8)
        val newMatch = match.copy(
            id = id,
            scorerId = if (match.scorerId.isNotEmpty()) match.scorerId else localUserId
        )
        val updated = listOf(newMatch) + _matchesFlow.value
        _matchesFlow.value = updated
        persistMatches(updated)
        return id
    }

    override suspend fun updateMatch(match: Match) {
        val currentList = _matchesFlow.value
        val updatedList = currentList.map { if (it.id == match.id) match else it }
        _matchesFlow.value = updatedList
        persistMatches(updatedList)
    }

    override suspend fun getCurrentUserId(): String = localUserId
}
