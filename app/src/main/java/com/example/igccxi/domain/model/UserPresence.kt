package com.example.igccxi.domain.model

data class UserPresence(
    val uid: String = "",
    val lastActiveAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.2.4",
    val platform: String = "Android"
)
