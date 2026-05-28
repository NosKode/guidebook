package com.guidebook.service

import com.guidebook.data.dto.PlaceCreateRequest
import com.guidebook.data.dto.PlaceUpdateRequest
import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlaceServiceTest {

    private val placeRepository: PlaceRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val fileStorageService: FileStorageService = mockk()

    private val service = PlaceService(
        placeRepository, categoryRepository, fileStorageService, "http://localhost:8080"
    )

    private val now = LocalDateTime.now()
    private val ownerId = UUID.randomUUID()
    private val otherId = UUID.randomUUID()
    private val placeId = UUID.randomUUID()

    private fun makeUser(id: UUID = ownerId, role: UserRole = UserRole.USER) = User(
        id = id, email = "u@test.com", passwordHash = "hash", displayName = null,
        role = role, createdAt = now, avatarPath = null
    )

    private fun makePlace(
        id: UUID = placeId,
        uploadedBy: UUID = ownerId,
        status: PlaceStatus = PlaceStatus.APPROVED
    ) = Place(
        id = id, name = "Test Place", address = null, latitude = null, longitude = null,
        categoryId = null, description = null, coverPath = null, uploadedBy = uploadedBy,
        status = status, rejectionReason = null, createdAt = now, updatedAt = now
    )

    @BeforeEach
    fun setUpStats() {
        coEvery { placeRepository.getReviewStatsBatch(any()) } returns emptyMap()
        coEvery { placeRepository.getPhotosCountBatch(any()) } returns emptyMap()
    }

    // ── createPlace ───────────────────────────────────────────────────────────

    @Test
    fun `createPlace by regular user sets status PENDING`() = runTest {
        val user = makeUser(role = UserRole.USER)
        coEvery { placeRepository.create(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            makePlace(status = PlaceStatus.PENDING)
        val dto = service.createPlace(user, PlaceCreateRequest("Test"))
        assertEquals("PENDING", dto.status)
        coVerify { placeRepository.create(any(), any(), any(), any(), any(), any(), any(), PlaceStatus.PENDING) }
    }

    @Test
    fun `createPlace by admin sets status APPROVED`() = runTest {
        val admin = makeUser(role = UserRole.ADMIN)
        coEvery { placeRepository.create(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            makePlace(status = PlaceStatus.APPROVED)
        val dto = service.createPlace(admin, PlaceCreateRequest("Test"))
        assertEquals("APPROVED", dto.status)
        coVerify { placeRepository.create(any(), any(), any(), any(), any(), any(), any(), PlaceStatus.APPROVED) }
    }

    @Test
    fun `createPlace trims whitespace from name`() = runTest {
        coEvery { placeRepository.create("Trimmed Name", any(), any(), any(), any(), any(), any(), any()) } returns
            makePlace()
        service.createPlace(makeUser(), PlaceCreateRequest("  Trimmed Name  "))
        coVerify { placeRepository.create("Trimmed Name", any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createPlace trims whitespace from description`() = runTest {
        coEvery { placeRepository.create(any(), any(), any(), any(), any(), "trimmed desc", any(), any()) } returns
            makePlace()
        service.createPlace(makeUser(), PlaceCreateRequest("Test", description = "  trimmed desc  "))
        coVerify { placeRepository.create(any(), any(), any(), any(), any(), "trimmed desc", any(), any()) }
    }

    // ── getPlace ──────────────────────────────────────────────────────────────

    @Test
    fun `getPlace APPROVED accessible by anonymous caller`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace(status = PlaceStatus.APPROVED)
        val dto = service.getPlace(placeId, null)
        assertEquals(placeId.toString(), dto.id)
    }

    @Test
    fun `getPlace PENDING accessible by owner`() = runTest {
        val owner = makeUser(id = ownerId)
        coEvery { placeRepository.findById(placeId) } returns makePlace(status = PlaceStatus.PENDING, uploadedBy = ownerId)
        val dto = service.getPlace(placeId, owner)
        assertEquals(placeId.toString(), dto.id)
    }

    @Test
    fun `getPlace PENDING accessible by admin`() = runTest {
        val admin = makeUser(role = UserRole.ADMIN)
        coEvery { placeRepository.findById(placeId) } returns makePlace(status = PlaceStatus.PENDING)
        val dto = service.getPlace(placeId, admin)
        assertEquals(placeId.toString(), dto.id)
    }

    @Test
    fun `getPlace PENDING by anonymous throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace(status = PlaceStatus.PENDING)
        assertFailsWith<NotFoundException> { service.getPlace(placeId, null) }
    }

    @Test
    fun `getPlace PENDING by other user throws NotFoundException`() = runTest {
        val other = makeUser(id = otherId)
        coEvery { placeRepository.findById(placeId) } returns makePlace(status = PlaceStatus.PENDING, uploadedBy = ownerId)
        assertFailsWith<NotFoundException> { service.getPlace(placeId, other) }
    }

    @Test
    fun `getPlace not found throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> { service.getPlace(UUID.randomUUID(), null) }
    }

    // ── deletePlace ───────────────────────────────────────────────────────────

    @Test
    fun `deletePlace by owner calls repository delete`() = runTest {
        val owner = makeUser(id = ownerId)
        coEvery { placeRepository.findById(placeId) } returns makePlace(uploadedBy = ownerId)
        coEvery { placeRepository.delete(placeId) } returns true
        service.deletePlace(placeId, owner)
        coVerify { placeRepository.delete(placeId) }
    }

    @Test
    fun `deletePlace by admin calls repository delete regardless of owner`() = runTest {
        val admin = makeUser(id = UUID.randomUUID(), role = UserRole.ADMIN)
        coEvery { placeRepository.findById(placeId) } returns makePlace(uploadedBy = ownerId)
        coEvery { placeRepository.delete(placeId) } returns true
        service.deletePlace(placeId, admin)
        coVerify { placeRepository.delete(placeId) }
    }

    @Test
    fun `deletePlace by other user throws ForbiddenException`() = runTest {
        val other = makeUser(id = otherId)
        coEvery { placeRepository.findById(placeId) } returns makePlace(uploadedBy = ownerId)
        assertFailsWith<ForbiddenException> { service.deletePlace(placeId, other) }
    }

    @Test
    fun `deletePlace not found throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> { service.deletePlace(UUID.randomUUID(), makeUser()) }
    }

    // ── getPendingPlaces ──────────────────────────────────────────────────────

    @Test
    fun `getPendingPlaces by admin returns list of pending places`() = runTest {
        val admin = makeUser(role = UserRole.ADMIN)
        coEvery { placeRepository.findPending() } returns listOf(
            makePlace(status = PlaceStatus.PENDING),
            makePlace(id = UUID.randomUUID(), status = PlaceStatus.PENDING)
        )
        val result = service.getPendingPlaces(admin)
        assertEquals(2, result.size)
    }

    @Test
    fun `getPendingPlaces by regular user throws ForbiddenException`() = runTest {
        val user = makeUser(role = UserRole.USER)
        assertFailsWith<ForbiddenException> { service.getPendingPlaces(user) }
    }

    // ── updatePlace ───────────────────────────────────────────────────────────

    @Test
    fun `updatePlace not found throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> {
            service.updatePlace(UUID.randomUUID(), makeUser(), PlaceUpdateRequest(name = "New"))
        }
    }

    @Test
    fun `updatePlace by non-owner throws ForbiddenException`() = runTest {
        val other = makeUser(id = otherId)
        coEvery { placeRepository.findById(placeId) } returns makePlace(uploadedBy = ownerId)
        assertFailsWith<ForbiddenException> {
            service.updatePlace(placeId, other, PlaceUpdateRequest(name = "New"))
        }
    }

    @Test
    fun `updatePlace by owner calls repository update`() = runTest {
        val owner = makeUser(id = ownerId)
        val updated = makePlace()
        coEvery { placeRepository.findById(placeId) } returns makePlace(uploadedBy = ownerId)
        coEvery { placeRepository.update(placeId, "New Name", any(), any(), any(), any(), any()) } returns updated
        service.updatePlace(placeId, owner, PlaceUpdateRequest(name = "New Name"))
        coVerify { placeRepository.update(placeId, "New Name", any(), any(), any(), any(), any()) }
    }
}
