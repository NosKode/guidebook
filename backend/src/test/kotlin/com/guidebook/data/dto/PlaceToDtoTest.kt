package com.guidebook.data.dto

import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaceToDtoTest {

    private val now = LocalDateTime.now()
    private val placeId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun makePlace(
        coverPath: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        status: PlaceStatus = PlaceStatus.APPROVED
    ) = Place(
        id = placeId,
        name = "Test Place",
        address = "Street 1",
        latitude = latitude,
        longitude = longitude,
        categoryId = null,
        description = "desc",
        coverPath = coverPath,
        uploadedBy = userId,
        status = status,
        rejectionReason = null,
        createdAt = now,
        updatedAt = now
    )

    // ── coverUrl ──────────────────────────────────────────────────────────────

    @Test
    fun `coverUrl with coverPath uses baseUrl`() {
        val dto = makePlace(coverPath = "cover.jpg").toDto(baseUrl = "http://localhost:8080")
        assertEquals("http://localhost:8080/files/images/cover.jpg", dto.coverUrl)
    }

    @Test
    fun `coverUrl with coverPath and empty baseUrl returns path directly`() {
        val dto = makePlace(coverPath = "cover.jpg").toDto(baseUrl = "")
        assertEquals("cover.jpg", dto.coverUrl)
    }

    @Test
    fun `coverUrl without coverPath but with coordinates returns Yandex static map URL`() {
        val dto = makePlace(latitude = 55.75, longitude = 37.61).toDto(baseUrl = "http://localhost:8080")
        assertNotNull(dto.coverUrl)
        assertTrue(dto.coverUrl!!.contains("static-maps.yandex.ru"))
        assertTrue(dto.coverUrl!!.contains("55.75"))
        assertTrue(dto.coverUrl!!.contains("37.61"))
    }

    @Test
    fun `coverUrl without coverPath or coordinates is null`() {
        val dto = makePlace().toDto(baseUrl = "http://localhost:8080")
        assertNull(dto.coverUrl)
    }

    @Test
    fun `coverUrl with staticMapsKey uses v1 API with apikey param`() {
        val dto = makePlace(latitude = 55.75, longitude = 37.61)
            .toDto(baseUrl = "http://localhost:8080", staticMapsKey = "my-api-key")
        assertNotNull(dto.coverUrl)
        assertTrue(dto.coverUrl!!.contains("/v1?"))
        assertTrue(dto.coverUrl!!.contains("apikey=my-api-key"))
    }

    @Test
    fun `coverUrl without staticMapsKey uses legacy 1x API`() {
        val dto = makePlace(latitude = 55.75, longitude = 37.61)
            .toDto(baseUrl = "http://localhost:8080", staticMapsKey = "")
        assertNotNull(dto.coverUrl)
        assertTrue(dto.coverUrl!!.contains("/1.x/"))
        assertFalse(dto.coverUrl!!.contains("apikey"))
    }

    @Test
    fun `coverPath takes priority over coordinates for coverUrl`() {
        val dto = makePlace(coverPath = "cover.jpg", latitude = 55.75, longitude = 37.61)
            .toDto(baseUrl = "http://localhost:8080")
        assertTrue(dto.coverUrl!!.contains("cover.jpg"))
        assertFalse(dto.coverUrl!!.contains("yandex"))
    }

    @Test
    fun `coverUrl needs both latitude and longitude - only latitude gives null`() {
        val dto = makePlace(latitude = 55.75, longitude = null).toDto(baseUrl = "http://localhost:8080")
        assertNull(dto.coverUrl)
    }

    // ── Other fields ─────────────────────────────────────────────────────────

    @Test
    fun `status serialized as enum name`() {
        assertEquals("APPROVED", makePlace(status = PlaceStatus.APPROVED).toDto().status)
        assertEquals("PENDING", makePlace(status = PlaceStatus.PENDING).toDto().status)
        assertEquals("REJECTED", makePlace(status = PlaceStatus.REJECTED).toDto().status)
    }

    @Test
    fun `uploadedBy serialized as string UUID`() {
        val dto = makePlace().toDto()
        assertEquals(userId.toString(), dto.uploadedBy)
    }

    @Test
    fun `id serialized as string UUID`() {
        val dto = makePlace().toDto()
        assertEquals(placeId.toString(), dto.id)
    }

    @Test
    fun `averageRating reviewsCount and photosCount passed through correctly`() {
        val dto = makePlace().toDto(averageRating = 4.5, reviewsCount = 12, photosCount = 3)
        assertEquals(4.5, dto.averageRating, 0.001)
        assertEquals(12, dto.reviewsCount)
        assertEquals(3, dto.photosCount)
    }

    @Test
    fun `categoryName passed through correctly`() {
        val dto = makePlace().toDto(categoryName = "Парки")
        assertEquals("Парки", dto.categoryName)
    }

    @Test
    fun `defaults give zero stats and null categoryName`() {
        val dto = makePlace().toDto()
        assertEquals(0.0, dto.averageRating, 0.001)
        assertEquals(0, dto.reviewsCount)
        assertEquals(0, dto.photosCount)
        assertNull(dto.categoryName)
    }
}
