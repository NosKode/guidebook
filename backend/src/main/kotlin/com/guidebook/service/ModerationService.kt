package com.guidebook.service

import com.guidebook.data.dto.PlaceDto
import com.guidebook.data.dto.toDto
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.PlaceStatus
import java.util.UUID

class ModerationService(
    private val placeRepository: PlaceRepository,
    private val baseUrl: String,
    private val staticMapsKey: String = ""
) {

    suspend fun listPending(): List<PlaceDto> =
        placeRepository.findPending().map { it.toDto(baseUrl = baseUrl, staticMapsKey = staticMapsKey) }

    suspend fun approve(placeId: UUID): PlaceDto {
        val place = placeRepository.updateStatus(placeId, PlaceStatus.APPROVED)
            ?: throw NotFoundException("Place not found")
        return place.toDto(baseUrl = baseUrl, staticMapsKey = staticMapsKey)
    }

    suspend fun reject(placeId: UUID, reason: String?): PlaceDto {
        val place = placeRepository.reject(placeId, reason)
            ?: throw NotFoundException("Place not found")
        return place.toDto(baseUrl = baseUrl, staticMapsKey = staticMapsKey)
    }
}
