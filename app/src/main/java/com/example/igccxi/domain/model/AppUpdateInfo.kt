package com.example.igccxi.domain.model

/**
 * Domain model representing published metadata for an application update.
 */
data class AppUpdateInfo(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val isMandatory: Boolean = false,
    val minSupportedVersionCode: Int = 1,
    val fileSizeBytes: Long = 0L,
    val releaseDate: String = ""
)
