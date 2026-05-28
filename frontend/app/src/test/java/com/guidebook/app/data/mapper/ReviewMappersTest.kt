package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.ReviewDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewMappersTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = ReviewDto(
            id = "rev-1",
            placeId = "place-1",
            userId = "user-1",
            userName = "John",
            userAvatarUrl = "http://example.com/avatar.jpg",
            rating = 5,
            comment = "Great place!",
            createdAt = "2024-01-01T00:00:00Z"
        )
        val domain = dto.toDomain()
        assertEquals("rev-1", domain.id)
        assertEquals("place-1", domain.placeId)
        assertEquals("user-1", domain.userId)
        assertEquals("John", domain.userName)
        assertEquals("http://example.com/avatar.jpg", domain.userAvatarUrl)
        assertEquals(5, domain.rating)
        assertEquals("Great place!", domain.comment)
        assertEquals("2024-01-01T00:00:00Z", domain.createdAt)
    }

    @Test
    fun `toDomain handles nullable fields`() {
        val dto = ReviewDto(
            id = "rev-2",
            placeId = "place-1",
            userId = "user-2",
            userName = null,
            userAvatarUrl = null,
            rating = 3,
            comment = null,
            createdAt = "2024-06-01T12:00:00Z"
        )
        val domain = dto.toDomain()
        assertNull(domain.userName)
        assertNull(domain.userAvatarUrl)
        assertNull(domain.comment)
    }

    @Test
    fun `toDomain maps minimum rating`() {
        val dto = ReviewDto("r", "p", "u", null, null, 1, null, "2024-01-01")
        assertEquals(1, dto.toDomain().rating)
    }

    @Test
    fun `toDomain maps maximum rating`() {
        val dto = ReviewDto("r", "p", "u", null, null, 5, null, "2024-01-01")
        assertEquals(5, dto.toDomain().rating)
    }
}
