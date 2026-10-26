package com.example.igccxi.domain.usecase

import com.example.igccxi.domain.model.AppUpdateInfo
import com.example.igccxi.domain.model.UpdateDownloadState
import com.example.igccxi.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakeAppUpdateRepository(
    var currentCode: Int = 1,
    var currentName: String = "1.0",
    var updateInfoToReturn: AppUpdateInfo? = null
) : AppUpdateRepository {

    override fun getCurrentVersionCode(): Int = currentCode
    override fun getCurrentVersionName(): String = currentName
    override suspend fun fetchLatestUpdateInfo(): AppUpdateInfo? = updateInfoToReturn
    override fun downloadUpdateApk(downloadUrl: String): Flow<UpdateDownloadState> = emptyFlow()
    override fun canRequestPackageInstalls(): Boolean = true
    override fun launchInstallPermissionSettings() {}
    override fun installApk(apkFile: File): Result<Unit> = Result.success(Unit)
}

class CheckForAppUpdateUseCaseTest {

    @Test
    fun testNoUpdateWhenLatestVersionCodeIsSameOrLower() = runBlocking {
        val repo = FakeAppUpdateRepository(
            currentCode = 2,
            currentName = "1.1.0",
            updateInfoToReturn = AppUpdateInfo(
                latestVersionCode = 2,
                latestVersionName = "1.1.0"
            )
        )
        val useCase = CheckForAppUpdateUseCase(repo)
        val result = useCase()

        assertFalse(result.isUpdateAvailable)
        assertFalse(result.isMandatory)
        assertEquals(2, result.currentVersionCode)
        assertEquals("1.1.0", result.currentVersionName)
    }

    @Test
    fun testUpdateAvailableWhenLatestVersionCodeIsHigher() = runBlocking {
        val repo = FakeAppUpdateRepository(
            currentCode = 1,
            currentName = "1.0",
            updateInfoToReturn = AppUpdateInfo(
                latestVersionCode = 2,
                latestVersionName = "1.1.0",
                releaseNotes = "• New Leaderboard\n• In-App Auto Updater",
                downloadUrl = "https://example.com/app-v1.1.apk",
                isMandatory = false
            )
        )
        val useCase = CheckForAppUpdateUseCase(repo)
        val result = useCase()

        assertTrue(result.isUpdateAvailable)
        assertFalse(result.isMandatory)
        assertNotNull(result.updateInfo)
        assertEquals("1.1.0", result.updateInfo?.latestVersionName)
        assertEquals(2, result.updateInfo?.latestVersionCode)
    }

    @Test
    fun testMandatoryUpdateEnforcedWhenBelowMinSupportedVersion() = runBlocking {
        val repo = FakeAppUpdateRepository(
            currentCode = 1,
            currentName = "1.0",
            updateInfoToReturn = AppUpdateInfo(
                latestVersionCode = 3,
                latestVersionName = "1.3.0",
                isMandatory = false,
                minSupportedVersionCode = 2 // Current is 1, which is below 2
            )
        )
        val useCase = CheckForAppUpdateUseCase(repo)
        val result = useCase()

        assertTrue(result.isUpdateAvailable)
        assertTrue(result.isMandatory)
    }

    @Test
    fun testExplicitMandatoryFlagHonored() = runBlocking {
        val repo = FakeAppUpdateRepository(
            currentCode = 1,
            currentName = "1.0",
            updateInfoToReturn = AppUpdateInfo(
                latestVersionCode = 2,
                latestVersionName = "1.1.0",
                isMandatory = true
            )
        )
        val useCase = CheckForAppUpdateUseCase(repo)
        val result = useCase()

        assertTrue(result.isUpdateAvailable)
        assertTrue(result.isMandatory)
    }

    @Test
    fun testNullUpdateInfoReturnsSafeDefaults() = runBlocking {
        val repo = FakeAppUpdateRepository(
            currentCode = 1,
            currentName = "1.0",
            updateInfoToReturn = null
        )
        val useCase = CheckForAppUpdateUseCase(repo)
        val result = useCase()

        assertFalse(result.isUpdateAvailable)
        assertFalse(result.isMandatory)
        assertNull(result.updateInfo)
    }
}
