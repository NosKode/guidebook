package com.guidebook.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Photo(
    val id: UUID,
    val placeId: UUID,
    val filePath: String,
    val caption: String?,
    val createdAt: LocalDateTime
)
