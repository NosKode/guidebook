package com.guidebook.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Favorite(
    val userId: UUID,
    val placeId: UUID,
    val addedAt: LocalDateTime
)
