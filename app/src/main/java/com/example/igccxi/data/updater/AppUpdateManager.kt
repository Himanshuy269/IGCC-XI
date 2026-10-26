package com.example.igccxi.data.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.model.UpdateDownloadState
import com.example.igccxi.domain.repository.AppUpdateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(
    private val context: Context
) : AppUpdateRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                com.google.firebase.FirebaseApp.initializeApp(context)?.let {
                    FirebaseFirestore.getInstance()
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firebase unavailable for update fallback: ${t.message}")
            null
        }

    companion object {
        private const val TAG = "AppUpdateManager"
        const val GITHUB_OWNER = "Himanshuy269"
        const val GITHUB_REPO = "IGCC-XI"
        const val GITHUB_JSDELIVR_MANIFEST_URL = "https://cdn.jsdelivr.net/gh/$GITHUB_OWNER/$GITHUB_REPO@main/app-update.json"
        const val GITHUB_RAW_MANIFEST_URL = "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/app-update.json"
        const val GITHUB_RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        const val GITHUB_FALLBACK_DOWNLOAD_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/latest/download/IGCC-XI.apk"
    }

    override fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (_: Exception) {
            1
        }
    }

    override fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    override suspend fun fetchLatestUpdateInfo(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        // Strategy 1: Check jsDelivr CDN Mirror (Global high-availability, bypasses ISP blocks on raw.githubusercontent.com)
        try {
            val jsDelivrInfo = fetchFromManifestUrl(GITHUB_JSDELIVR_MANIFEST_URL)
            if (jsDelivrInfo != null) {
                Log.d(TAG, "Successfully fetched update info from jsDelivr CDN (v${jsDelivrInfo.latestVersionName})")
                return@withContext jsDelivrInfo
            }
        } catch (e: Exception) {
            Log.w(TAG, "jsDelivr fetch failed: ${e.message}")
        }

        // Strategy 2: Check GitHub Raw Repository Manifest
        try {
            val rawGitHubInfo = fetchFromManifestUrl(GITHUB_RAW_MANIFEST_URL)
            if (rawGitHubInfo != null) {
                Log.d(TAG, "Successfully fetched update info from GitHub Raw (v${rawGitHubInfo.latestVersionName})")
                return@withContext rawGitHubInfo
            }
        } catch (e: Exception) {
            Log.w(TAG, "GitHub Raw fetch failed: ${e.message}")
        }

        // Strategy 3: Check GitHub Releases API
        try {
            val apiGitHubInfo = fetchFromGitHubReleasesApi()
            if (apiGitHubInfo != null) {
                Log.d(TAG, "Successfully fetched update info from GitHub Releases API (v${apiGitHubInfo.latestVersionName})")
                return@withContext apiGitHubInfo
            }
        } catch (e: Exception) {
            Log.w(TAG, "GitHub Releases API fetch failed: ${e.message}")
        }

        // Strategy 4: Check Cloud Firestore as secondary fallback
        try {
            val fs = firestore
            if (fs != null) {
                val doc = fs.collection("app_metadata").document("app_update").get().await()
                if (doc != null && doc.exists()) {
                    val latestCode = doc.getLong("latestVersionCode")?.toInt() ?: 1
                    val latestName = doc.getString("latestVersionName") ?: "1.0"
                    val notes = doc.getString("releaseNotes") ?: ""
                    val url = doc.getString("downloadUrl") ?: GITHUB_FALLBACK_DOWNLOAD_URL
                    val mandatory = doc.getBoolean("isMandatory") ?: false
                    val minCode = doc.getLong("minSupportedVersionCode")?.toInt() ?: 1
                    val size = doc.getLong("fileSizeBytes") ?: 0L
                    val date = doc.getString("releaseDate") ?: ""

                    Log.d(TAG, "Successfully fetched update info from Firestore fallback (v$latestName)")
                    return@withContext AppUpdateInfo(
                        latestVersionCode = latestCode,
                        latestVersionName = latestName,
                        releaseNotes = notes,
                        downloadUrl = url,
                        isMandatory = mandatory,
                        minSupportedVersionCode = minCode,
                        fileSizeBytes = size,
                        releaseDate = date
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore fallback fetch failed: ${e.message}")
        }

        null
    }

    private fun fetchFromManifestUrl(targetUrl: String): AppUpdateInfo? {
        val url = URL(targetUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) IGCC-XI")
            setRequestProperty("Accept", "*/*")
        }
        return try {
            val code = connection.responseCode
            if (code in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(responseText)
                val versionCode = obj.optInt("latestVersionCode", 1)
                val versionName = obj.optString("latestVersionName", "1.0")
                val notes = obj.optString("releaseNotes", "")
                var downloadUrl = obj.optString("downloadUrl", "")
                if (downloadUrl.isBlank()) {
                    downloadUrl = GITHUB_FALLBACK_DOWNLOAD_URL
                }
                val isMandatory = obj.optBoolean("isMandatory", false)
                val minCode = obj.optInt("minSupportedVersionCode", 1)
                val size = obj.optLong("fileSizeBytes", 0L)
                val date = obj.optString("releaseDate", "")

                AppUpdateInfo(
                    latestVersionCode = versionCode,
                    latestVersionName = versionName,
                    releaseNotes = notes,
                    downloadUrl = downloadUrl,
                    isMandatory = isMandatory,
                    minSupportedVersionCode = minCode,
                    fileSizeBytes = size,
                    releaseDate = date
                )
            } else {
                Log.w(TAG, "HTTP $code from $targetUrl")
                null
            }
        } finally {
            try { connection.disconnect() } catch (_: Exception) {}
        }
    }

    private fun fetchFromGitHubReleasesApi(): AppUpdateInfo? {
        val url = URL(GITHUB_RELEASES_API_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) IGCC-XI")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
        }
        return try {
            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(responseText)
                val rawTag = obj.optString("tag_name", "v1.0")
                val versionName = rawTag.removePrefix("v").removePrefix("V")
                val body = obj.optString("body", "")
                val publishedAt = obj.optString("published_at", obj.optString("created_at", ""))

                var downloadUrl = ""
                var fileSize = 0L
                val assets = obj.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            fileSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }
                if (downloadUrl.isBlank()) {
                    downloadUrl = GITHUB_FALLBACK_DOWNLOAD_URL
                }

                val calculatedCode = parseVersionCodeFromTag(versionName)

                AppUpdateInfo(
                    latestVersionCode = calculatedCode,
                    latestVersionName = versionName,
                    releaseNotes = body.ifBlank { "New release $versionName is available on GitHub." },
                    downloadUrl = downloadUrl,
                    isMandatory = false,
                    minSupportedVersionCode = 1,
                    fileSizeBytes = fileSize,
                    releaseDate = publishedAt.take(10)
                )
            } else {
                null
            }
        } finally {
            try { connection.disconnect() } catch (_: Exception) {}
        }
    }

    private fun parseVersionCodeFromTag(tag: String): Int {
        val clean = tag.filter { it.isDigit() || it == '.' }
        val parts = clean.split(".").mapNotNull { it.toIntOrNull() }
        return when {
            parts.size >= 3 -> parts[0] * 10000 + parts[1] * 100 + parts[2]
            parts.size == 2 -> parts[0] * 100 + parts[1]
            parts.size == 1 -> parts[0]
            else -> 2
        }
    }

    override fun downloadUpdateApk(downloadUrl: String): Flow<UpdateDownloadState> = flow {
        emit(UpdateDownloadState.Downloading(progressPercent = 0, bytesDownloaded = 0L, totalBytes = 0L))

        if (downloadUrl.isBlank()) {
            emit(UpdateDownloadState.Error("Invalid or empty download URL from GitHub."))
            return@flow
        }

        val updateDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
        if (!updateDir.exists()) {
            updateDir.mkdirs()
        }
        val tempApk = File(updateDir, "IGCCXI_Update.apk.tmp")
        val targetApk = File(updateDir, "IGCCXI_Update.apk")
        if (tempApk.exists()) {
            tempApk.delete()
        }

        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            var currentUrl = downloadUrl
            var redirects = 0
            while (redirects < 5) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) IGCC-XI")
                    connect()
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == 307 || code == 308
                ) {
                    currentUrl = connection.getHeaderField("Location") ?: break
                    redirects++
                } else {
                    break
                }
            }

            val responseCode = connection?.responseCode ?: -1
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                emit(UpdateDownloadState.Error("GitHub Release APK was not found (HTTP 404). Please ensure the release workflow has finished creating the release on GitHub."))
                return@flow
            }

            if (responseCode !in 200..299) {
                emit(UpdateDownloadState.Error("GitHub server returned HTTP $responseCode while downloading update."))
                return@flow
            }

            val totalBytes = connection?.contentLengthLong ?: -1L
            inputStream = connection?.inputStream ?: throw IllegalStateException("Cannot open stream")
            outputStream = FileOutputStream(tempApk)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalDownloaded = 0L
            var lastEmittedPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalDownloaded += bytesRead

                val percent = if (totalBytes > 0) {
                    ((totalDownloaded * 100) / totalBytes).toInt()
                } else {
                    0
                }

                if (percent != lastEmittedPercent || percent == 100) {
                    lastEmittedPercent = percent
                    emit(
                        UpdateDownloadState.Downloading(
                            progressPercent = percent,
                            bytesDownloaded = totalDownloaded,
                            totalBytes = totalBytes
                        )
                    )
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null

            // Atomic rename to target APK so corrupted partial files never trigger install
            if (targetApk.exists()) {
                targetApk.delete()
            }
            val renamed = tempApk.renameTo(targetApk)
            val readyApk = if (renamed && targetApk.exists()) {
                targetApk
            } else {
                tempApk.copyTo(targetApk, overwrite = true)
                tempApk.delete()
                targetApk
            }

            emit(UpdateDownloadState.ReadyToInstall(readyApk))
        } catch (e: Exception) {
            if (tempApk.exists()) {
                try { tempApk.delete() } catch (_: Exception) {}
            }
            emit(UpdateDownloadState.Error(e.localizedMessage ?: "Download from GitHub failed. Please check your internet connection."))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { inputStream?.close() } catch (_: Exception) {}
            connection?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    override fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    override fun launchInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(IllegalStateException("Downloaded APK file is empty or does not exist"))
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Explicitly grant read permissions to all intent handlers (package installers)
            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    installIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(installIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            for (resolveInfo in resolveInfoList) {
                val targetPackage = resolveInfo.activityInfo.packageName
                context.grantUriPermission(targetPackage, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
