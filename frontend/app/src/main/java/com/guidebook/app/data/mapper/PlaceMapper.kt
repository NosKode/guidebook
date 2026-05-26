package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.PagedResponse
import com.guidebook.app.data.remote.dto.PlaceDto
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.PlaceStatus

fun PlaceDto.toDomain(): Place = Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    categoryId = categoryId,
    categoryName = categoryName,
    description = description,
    coverUrl = coverUrl,
    uploadedBy = uploadedBy,
    status = runCatching { PlaceStatus.valueOf(status) }.getOrDefault(PlaceStatus.PENDING),
    rejectionReason = rejectionReason,
    averageRating = averageRating,
    reviewsCount = reviewsCount,
    photosCount = photosCount,
    createdAt = createdAt
)

fun PagedResponse<PlaceDto>.toDomain(): PagedData<Place> = PagedData(
    items = items.map { it.toDomain() },
    page = page,
    pageSize = pageSize,
    totalItems = totalItems,
    totalPages = totalPages
)
