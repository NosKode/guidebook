package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.PhotoDto
import com.guidebook.app.domain.model.Photo

fun PhotoDto.toDomain(): Photo = Photo(
    id = id,
    placeId = placeId,
    photoUrl = photoUrl,
    caption = caption,
    createdAt = createdAt
)
