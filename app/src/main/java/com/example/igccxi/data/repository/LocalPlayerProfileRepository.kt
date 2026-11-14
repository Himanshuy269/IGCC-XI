package com.example.igccxi.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.PlayerProfile
import com.example.igccxi.domain.repository.MatchRepository
import com.example.igccxi.domain.repository.PlayerProfileRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

class LocalPlayerProfileRepository(
    private val context: Context,
    private val matchRepository: MatchRepository? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PlayerProfileRepository {

    private val gson = Gson()
    private val cacheFile = File(context.filesDir, "player_profiles.json")
    private val photosDir = File(context.filesDir, "player_profiles").apply { mkdirs() }

    private val _profilesFlow = MutableStateFlow<List<PlayerProfile>>(loadPersistedProfiles())

    companion object {
        private val AVATAR_COLORS = listOf(
            0xFF1B5E20, // Green
            0xFF0D47A1, // Blue
            0xFFB71C1C, // Red
            0xFF4A148C, // Purple
            0xFFE65100, // Orange
            0xFF006064, // Cyan
            0xFF311B92, // Deep Purple
            0xFF827717  // Olive / Gold
        )

        fun assignColor(name: String): Long {
            val hash = name.trim().lowercase(Locale.ROOT).hashCode()
            val index = Math.abs(hash) % AVATAR_COLORS.size
            return AVATAR_COLORS[index]
        }
    }

    init {
        coroutineScope.launch {
            seedFromMatches()
        }
    }

    private fun loadPersistedProfiles(): List<PlayerProfile> {
        return try {
            if (cacheFile.exists() && cacheFile.length() > 0L) {
                val json = cacheFile.readText()
                val type = object : TypeToken<List<PlayerProfile>>() {}.type
                val list: List<PlayerProfile> = gson.fromJson(json, type) ?: emptyList()
                list.sortedBy { it.name.lowercase(Locale.ROOT) }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun persistProfiles(profiles: List<PlayerProfile>) {
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(profiles)
                cacheFile.writeText(json)
            } catch (_: Exception) {
                // Ignore persistence failures
            }
        }
    }

    override fun observeProfiles(): Flow<List<PlayerProfile>> = _profilesFlow.asStateFlow()

    override suspend fun getProfiles(): List<PlayerProfile> = _profilesFlow.value

    override suspend fun getProfileByName(name: String): PlayerProfile? {
        val clean = name.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return null
        return _profilesFlow.value.find { it.name.trim().lowercase(Locale.ROOT) == clean }
    }

    override suspend fun registerPlayerName(name: String): PlayerProfile {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return PlayerProfile(name = "")
        }

        val existing = getProfileByName(cleanName)
        if (existing != null) {
            return existing
        }

        val newProfile = PlayerProfile(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            avatarColorHex = assignColor(cleanName),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val updated = (_profilesFlow.value + newProfile).sortedBy { it.name.lowercase(Locale.ROOT) }
        _profilesFlow.value = updated
        persistProfiles(updated)
        return newProfile
    }

    override suspend fun saveOrUpdateProfile(profile: PlayerProfile): PlayerProfile {
        val cleanName = profile.name.trim()
        val current = _profilesFlow.value.toMutableList()
        val index = current.indexOfFirst {
            it.id == profile.id || it.name.trim().equals(cleanName, ignoreCase = true)
        }

        val updatedProfile = profile.copy(
            name = cleanName,
            updatedAt = System.currentTimeMillis()
        )

        if (index >= 0) {
            current[index] = updatedProfile
        } else {
            current.add(updatedProfile)
        }

        val sorted = current.sortedBy { it.name.lowercase(Locale.ROOT) }
        _profilesFlow.value = sorted
        persistProfiles(sorted)
        return updatedProfile
    }

    override suspend fun updateProfilePhoto(name: String, photoPath: String?, avatarIcon: String?): PlayerProfile? {
        val clean = name.trim()
        if (clean.isEmpty()) return null
        val existing = getProfileByName(clean) ?: registerPlayerName(clean)
        val updated = existing.copy(
            photoPath = photoPath,
            avatarIcon = avatarIcon,
            updatedAt = System.currentTimeMillis()
        )
        return saveOrUpdateProfile(updated)
    }

    override suspend fun deleteProfile(id: String) {
        val current = _profilesFlow.value.toMutableList()
        val target = current.find { it.id == id }
        if (target?.photoPath != null) {
            try {
                File(target.photoPath).delete()
            } catch (_: Exception) {}
        }
        current.removeAll { it.id == id }
        _profilesFlow.value = current
        persistProfiles(current)
    }

    /**
     * Copies and saves an image Uri from gallery into internal private files,
     * compressing it to avoid excessive memory usage.
     */
    suspend fun saveImageFromUri(playerName: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val cleanSlug = playerName.trim().lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")
            val targetFile = File(photosDir, "player_${cleanSlug}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                val originalBitmap = BitmapFactory.decodeStream(input) ?: return@withContext null
                
                // Scale down if larger than 512px
                val maxDim = 512
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDim || height > maxDim) {
                    val ratio = width.toFloat() / height.toFloat()
                    val targetW: Int
                    val targetH: Int
                    if (ratio > 1f) {
                        targetW = maxDim
                        targetH = (maxDim / ratio).toInt()
                    } else {
                        targetH = maxDim
                        targetW = (maxDim * ratio).toInt()
                    }
                    Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                } else {
                    originalBitmap
                }

                FileOutputStream(targetFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            }

            if (targetFile.exists() && targetFile.length() > 0L) {
                targetFile.absolutePath
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun seedFromMatches() {
        val repo = matchRepository ?: return
        try {
            val namesToRegister = mutableSetOf<String>()
            repo.observeMatches().collect { matches ->
                for (match in matches) {
                    val inningsList = listOfNotNull(match.innings1, match.innings2)
                    for (innings in inningsList) {
                        for (batter in innings.batters) {
                            val n = batter.name.trim()
                            if (n.isNotEmpty() && !n.startsWith("Striker", ignoreCase = true) && !n.startsWith("Non-Striker", ignoreCase = true)) {
                                namesToRegister.add(n)
                            }
                        }
                        for (bowler in innings.bowlers) {
                            val n = bowler.name.trim()
                            if (n.isNotEmpty() && !n.startsWith("Bowler", ignoreCase = true)) {
                                namesToRegister.add(n)
                            }
                        }
                    }
                }

                // Register all unique player names found in matches
                for (name in namesToRegister) {
                    if (getProfileByName(name) == null) {
                        registerPlayerName(name)
                    }
                }
            }
        } catch (_: Exception) {
            // Seeding non-critical
        }
    }
}
