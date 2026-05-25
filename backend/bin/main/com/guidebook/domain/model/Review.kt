package com.guidebook.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Review(
    val id: UUID,
    val placeId: UUID,
    val userId: UUID,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime
)
