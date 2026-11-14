package com.example.igccxi.domain.repository

import com.example.igccxi.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface PlayerProfileRepository {
    fun observeProfiles(): Flow<List<PlayerProfile>>
    suspend fun getProfiles(): List<PlayerProfile>
    suspend fun getProfileByName(name: String): PlayerProfile?
    suspend fun saveOrUpdateProfile(profile: PlayerProfile): PlayerProfile
    suspend fun registerPlayerName(name: String): PlayerProfile
    suspend fun updateProfilePhoto(name: String, photoPath: String?, avatarIcon: String? = null): PlayerProfile?
    suspend fun deleteProfile(id: String)
    suspend fun seedFromMatches()
}
