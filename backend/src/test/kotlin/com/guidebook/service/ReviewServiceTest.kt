package com.guidebook.service

import com.guidebook.data.dto.ReviewCreateRequest
import com.guidebook.data.dto.ReviewUpdateRequest
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.data.repository.ReviewRepository
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import com.guidebook.domain.model.Review
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReviewServiceTest {

    private val reviewRepository: ReviewRepository = mockk()
    private val placeRepository: PlaceRepository = mockk()

    private val service = ReviewService(reviewRepository, placeRepository, "http://localhost:8080")

    private val now = LocalDateTime.now()
    private val placeId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val reviewId = UUID.randomUUID()

    private fun makeUser(id: UUID = userId, role: UserRole = UserRole.USER) = User(
        id = id, email = "u@test.com", passwordHash = "hash", displayName = "Alice",
        role = role, createdAt = now, avatarPath = null
    )

    private fun makePlace() = Place(
        id = placeId, name = "Place", address = null, latitude = null, longitude = null,
        categoryId = null, description = null, coverPath = null, uploadedBy = userId,
        status = PlaceStatus.APPROVED, rejectionReason = null, createdAt = now, updatedAt = now
    )

    private fun makeReview(authorId: UUID = userId, rating: Int = 4) = Review(
        id = reviewId, placeId = placeId, userId = authorId,
        rating = rating, comment = "Good", createdAt = now
    )

    // ── createReview ──────────────────────────────────────────────────────────

    @Test
    fun `createReview with rating 0 throws BadRequestException`() = runTest {
        assertFailsWith<BadRequestException> {
            service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 0))
        }
    }

    @Test
    fun `createReview with rating 6 throws BadRequestException`() = runTest {
        assertFailsWith<BadRequestException> {
            service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 6))
        }
    }

    @Test
    fun `createReview with negative rating throws BadRequestException`() = runTest {
        assertFailsWith<BadRequestException> {
            service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = -1))
        }
    }

    @Test
    fun `createReview for non-existent place throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> {
            service.createReview(UUID.randomUUID(), makeUser(), ReviewCreateRequest(rating = 4))
        }
    }

    @Test
    fun `createReview when user already reviewed throws ConflictException`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByUserAndPlace(userId, placeId) } returns makeReview()
        assertFailsWith<ConflictException> {
            service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 4))
        }
    }

    @Test
    fun `createReview with rating 1 is valid boundary`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByUserAndPlace(userId, placeId) } returns null
        coEvery { reviewRepository.create(placeId, userId, 1, null) } returns makeReview(rating = 1)
        val dto = service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 1))
        assertEquals(1, dto.rating)
    }

    @Test
    fun `createReview with rating 5 is valid boundary`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByUserAndPlace(userId, placeId) } returns null
        coEvery { reviewRepository.create(placeId, userId, 5, null) } returns makeReview(rating = 5)
        val dto = service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 5))
        assertEquals(5, dto.rating)
    }

    @Test
    fun `createReview success returns ReviewDto with correct fields`() = runTest {
        val user = makeUser()
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByUserAndPlace(userId, placeId) } returns null
        coEvery { reviewRepository.create(placeId, userId, 4, "Great") } returns makeReview()
        val dto = service.createReview(placeId, user, ReviewCreateRequest(rating = 4, comment = "Great"))
        assertEquals(reviewId.toString(), dto.id)
        assertEquals(placeId.toString(), dto.placeId)
        assertEquals(4, dto.rating)
        assertEquals("Alice", dto.userName)
    }

    @Test
    fun `createReview trims comment whitespace`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByUserAndPlace(userId, placeId) } returns null
        coEvery { reviewRepository.create(placeId, userId, 4, "trimmed") } returns makeReview()
        service.createReview(placeId, makeUser(), ReviewCreateRequest(rating = 4, comment = "  trimmed  "))
        coVerify { reviewRepository.create(placeId, userId, 4, "trimmed") }
    }

    // ── updateReview ──────────────────────────────────────────────────────────

    @Test
    fun `updateReview not found throws NotFoundException`() = runTest {
        coEvery { reviewRepository.findById(reviewId) } returns null
        assertFailsWith<NotFoundException> {
            service.updateReview(reviewId, makeUser(), ReviewUpdateRequest(rating = 3))
        }
    }

    @Test
    fun `updateReview by non-author throws ForbiddenException`() = runTest {
        val other = makeUser(id = UUID.randomUUID())
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        assertFailsWith<ForbiddenException> {
            service.updateReview(reviewId, other, ReviewUpdateRequest(rating = 3))
        }
    }

    @Test
    fun `updateReview with invalid rating throws BadRequestException`() = runTest {
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        assertFailsWith<BadRequestException> {
            service.updateReview(reviewId, makeUser(id = userId), ReviewUpdateRequest(rating = 0))
        }
    }

    @Test
    fun `updateReview with rating 6 throws BadRequestException`() = runTest {
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        assertFailsWith<BadRequestException> {
            service.updateReview(reviewId, makeUser(id = userId), ReviewUpdateRequest(rating = 6))
        }
    }

    @Test
    fun `updateReview success returns updated ReviewDto`() = runTest {
        val user = makeUser(id = userId)
        val updated = makeReview(rating = 5)
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        coEvery { reviewRepository.update(reviewId, 5, null) } returns updated
        val dto = service.updateReview(reviewId, user, ReviewUpdateRequest(rating = 5))
        assertEquals(5, dto.rating)
    }

    // ── deleteReview ──────────────────────────────────────────────────────────

    @Test
    fun `deleteReview by author calls repository delete`() = runTest {
        val author = makeUser(id = userId)
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        coEvery { reviewRepository.delete(reviewId) } returns true
        service.deleteReview(reviewId, author)
        coVerify { reviewRepository.delete(reviewId) }
    }

    @Test
    fun `deleteReview by admin calls repository delete regardless of author`() = runTest {
        val admin = makeUser(id = UUID.randomUUID(), role = UserRole.ADMIN)
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        coEvery { reviewRepository.delete(reviewId) } returns true
        service.deleteReview(reviewId, admin)
        coVerify { reviewRepository.delete(reviewId) }
    }

    @Test
    fun `deleteReview by other user throws ForbiddenException`() = runTest {
        val other = makeUser(id = UUID.randomUUID(), role = UserRole.USER)
        coEvery { reviewRepository.findById(reviewId) } returns makeReview(authorId = userId)
        assertFailsWith<ForbiddenException> { service.deleteReview(reviewId, other) }
    }

    @Test
    fun `deleteReview not found throws NotFoundException`() = runTest {
        coEvery { reviewRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> { service.deleteReview(UUID.randomUUID(), makeUser()) }
    }

    // ── getReviewsForPlace ────────────────────────────────────────────────────

    @Test
    fun `getReviewsForPlace for non-existent place throws NotFoundException`() = runTest {
        coEvery { placeRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> { service.getReviewsForPlace(UUID.randomUUID()) }
    }

    @Test
    fun `getReviewsForPlace returns list of ReviewDto`() = runTest {
        coEvery { placeRepository.findById(placeId) } returns makePlace()
        coEvery { reviewRepository.findByPlace(placeId) } returns listOf(
            com.guidebook.data.repository.ReviewRow(makeReview(), "Alice", null),
            com.guidebook.data.repository.ReviewRow(makeReview().copy(id = UUID.randomUUID()), "Bob", null)
        )
        val result = service.getReviewsForPlace(placeId)
        assertEquals(2, result.size)
    }
}
