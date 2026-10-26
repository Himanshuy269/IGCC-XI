package com.example.igccxi.domain.usecase

import com.example.igccxi.data.repository.InMemoryPresenceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveActiveUsersUseCaseTest {

    @Test
    fun observeActiveUsers_emitsAtLeastOneUser() = runBlocking {
        val repository = InMemoryPresenceRepository()
        val useCase = ObserveActiveUsersUseCase(repository)

        val activeCount = useCase().first()
        assertTrue("Active users count must be at least 1 for the current device", activeCount >= 1)
        assertEquals(1, activeCount)
    }

    @Test
    fun recordHeartbeat_handlesUserIdGracefully() = runBlocking {
        val repository = InMemoryPresenceRepository()
        val useCase = ObserveActiveUsersUseCase(repository)

        // Should complete without exception
        useCase.recordHeartbeat("test_user_123")
        useCase.recordHeartbeat("")
    }
}
