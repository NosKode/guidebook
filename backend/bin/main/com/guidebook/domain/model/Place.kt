package com.guidebook.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Place(
    val id: UUID,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val categoryId: Int?,
    val description: String?,
    val coverPath: String?,
    val uploadedBy: UUID?,
    val status: PlaceStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
