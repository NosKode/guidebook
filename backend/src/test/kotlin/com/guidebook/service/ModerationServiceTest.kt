package com.guidebook.service

import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModerationServiceTest {

    private val placeRepository: PlaceRepository = mockk()
    private val service = ModerationService(placeRepository, "http://localhost:8080")

    private val now = LocalDateTime.now()
    private val placeId = UUID.randomUUID()

    private fun makePlace(status: PlaceStatus = PlaceStatus.PENDING, reason: String? = null) = Place(
        id = placeId, name = "Test", address = null, latitude = null, longitude = null,
        categoryId = null, description = null, coverPath = null, uploadedBy = UUID.randomUUID(),
        status = status, rejectionReason = reason, createdAt = now, updatedAt = now
    )

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    fun `approve returns PlaceDto with APPROVED status`() = runTest {
        coEvery { placeRepository.updateStatus(placeId, PlaceStatus.APPROVED) } returns
            makePlace(status = PlaceStatus.APPROVED)
        val dto = service.approve(placeId)
        assertEquals("APPROVED", dto.status)
    }

    @Test
    fun `approve for non-existent place throws NotFoundException`() = runTest {
        coEvery { placeRepository.updateStatus(any(), any()) } returns null
        assertFailsWith<NotFoundException> { service.approve(UUID.randomUUID()) }
    }

    @Test
    fun `approve returns correct place id`() = runTest {
        coEvery { placeRepository.updateStatus(placeId, PlaceStatus.APPROVED) } returns
            makePlace(status = PlaceStatus.APPROVED)
        val dto = service.approve(placeId)
        assertEquals(placeId.toString(), dto.id)
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Test
    fun `reject returns PlaceDto with REJECTED status`() = runTest {
        coEvery { placeRepository.reject(placeId, "Bad content") } returns
            makePlace(status = PlaceStatus.REJECTED, reason = "Bad content")
        val dto = service.reject(placeId, "Bad content")
        assertEquals("REJECTED", dto.status)
    }

    @Test
    fun `reject for non-existent place throws NotFoundException`() = runTest {
        coEvery { placeRepository.reject(any(), any()) } returns null
        assertFailsWith<NotFoundException> { service.reject(UUID.randomUUID(), "reason") }
    }

    @Test
    fun `reject with null reason is allowed`() = runTest {
        coEvery { placeRepository.reject(placeId, null) } returns
            makePlace(status = PlaceStatus.REJECTED)
        val dto = service.reject(placeId, null)
        assertEquals("REJECTED", dto.status)
    }

    // ── listPending ───────────────────────────────────────────────────────────

    @Test
    fun `listPending returns all pending places`() = runTest {
        coEvery { placeRepository.findPending() } returns listOf(
            makePlace(), makePlace().copy(id = UUID.randomUUID())
        )
        val result = service.listPending()
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == "PENDING" })
    }

    @Test
    fun `listPending returns empty list when no pending places`() = runTest {
        coEvery { placeRepository.findPending() } returns emptyList()
        val result = service.listPending()
        assertTrue(result.isEmpty())
    }
}
