package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.ReviewDto
import com.guidebook.app.domain.model.Review

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    placeId = placeId,
    userId = userId,
    userName = userName,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)
