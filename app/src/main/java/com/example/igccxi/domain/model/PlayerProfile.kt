package com.example.igccxi.domain.model

import java.util.UUID

/**
 * Domain model representing a persistent player profile stored in the app.
 * Contains the player's canonical name, local profile photo path (or preset avatar),
 * and basic metadata so the player is suggested automatically across matches.
 */
data class PlayerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val photoPath: String? = null,
    val avatarIcon: String? = null, // e.g. "🏏", "⚾", "👑", "🧤", "⚡", "🔥"
    val avatarColorHex: Long = 0xFF1B5E20,
    val role: String = "Player",
    val matchesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = name.trim()

    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.isEmpty() -> "P"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            }
        }
}
