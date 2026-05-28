package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.PagedResponse
import com.guidebook.app.data.remote.dto.PlaceDto
import com.guidebook.app.domain.model.PlaceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceMappersTest {

    private fun makeDto(status: String = "APPROVED") = PlaceDto(
        id = "place-1",
        name = "Test Place",
        address = "Street 1",
        latitude = 55.75,
        longitude = 37.61,
        categoryId = 2,
        categoryName = "Park",
        description = "Nice park",
        coverUrl = "http://example.com/cover.jpg",
        uploadedBy = "user-1",
        status = status,
        rejectionReason = null,
        averageRating = 4.5,
        reviewsCount = 10,
        photosCount = 5,
        createdAt = "2024-01-01T00:00:00Z"
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val domain = makeDto().toDomain()
        assertEquals("place-1", domain.id)
        assertEquals("Test Place", domain.name)
        assertEquals("Street 1", domain.address)
        assertEquals(55.75, domain.latitude!!, 0.001)
        assertEquals(37.61, domain.longitude!!, 0.001)
        assertEquals(2, domain.categoryId)
        assertEquals("Park", domain.categoryName)
        assertEquals("Nice park", domain.description)
        assertEquals("http://example.com/cover.jpg", domain.coverUrl)
        assertEquals("user-1", domain.uploadedBy)
        assertEquals(PlaceStatus.APPROVED, domain.status)
        assertNull(domain.rejectionReason)
        assertEquals(4.5, domain.averageRating, 0.001)
        assertEquals(10, domain.reviewsCount)
        assertEquals(5, domain.photosCount)
        assertEquals("2024-01-01T00:00:00Z", domain.createdAt)
    }

    @Test
    fun `toDomain maps PENDING status`() {
        assertEquals(PlaceStatus.PENDING, makeDto("PENDING").toDomain().status)
    }

    @Test
    fun `toDomain maps REJECTED status`() {
        assertEquals(PlaceStatus.REJECTED, makeDto("REJECTED").toDomain().status)
    }

    @Test
    fun `toDomain falls back to PENDING for unknown status string`() {
        assertEquals(PlaceStatus.PENDING, makeDto("UNKNOWN").toDomain().status)
    }

    @Test
    fun `toDomain handles all nullable fields as null`() {
        val dto = makeDto().copy(
            address = null,
            latitude = null,
            longitude = null,
            categoryId = null,
            categoryName = null,
            description = null,
            coverUrl = null,
            uploadedBy = null,
            rejectionReason = null
        )
        val domain = dto.toDomain()
        assertNull(domain.address)
        assertNull(domain.latitude)
        assertNull(domain.longitude)
        assertNull(domain.categoryId)
        assertNull(domain.categoryName)
        assertNull(domain.description)
        assertNull(domain.coverUrl)
        assertNull(domain.uploadedBy)
        assertNull(domain.rejectionReason)
    }

    @Test
    fun `PagedResponse toDomain maps items and pagination metadata`() {
        val paged = PagedResponse(
            items = listOf(makeDto(), makeDto().copy(id = "place-2")),
            page = 2,
            pageSize = 20,
            totalItems = 42L,
            totalPages = 3
        )
        val domain = paged.toDomain()
        assertEquals(2, domain.items.size)
        assertEquals("place-1", domain.items[0].id)
        assertEquals("place-2", domain.items[1].id)
        assertEquals(2, domain.page)
        assertEquals(20, domain.pageSize)
        assertEquals(42L, domain.totalItems)
        assertEquals(3, domain.totalPages)
    }

    @Test
    fun `PagedResponse toDomain with empty list`() {
        val paged = PagedResponse<PlaceDto>(
            items = emptyList(), page = 1, pageSize = 20, totalItems = 0L, totalPages = 0
        )
        assertTrue(paged.toDomain().items.isEmpty())
    }

    @Test
    fun `toDomain preserves zero averageRating`() {
        assertEquals(0.0, makeDto().copy(averageRating = 0.0).toDomain().averageRating, 0.001)
    }
}
