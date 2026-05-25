package com.guidebook.data.dto

import com.guidebook.domain.model.Place
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val categoryId: Int?,
    val categoryName: String?,
    val description: String?,
    val coverUrl: String?,
    val uploadedBy: String?,
    val status: String,
    val rejectionReason: String?,
    val averageRating: Double,
    val reviewsCount: Int,
    val photosCount: Int,
    val createdAt: String
)

@Serializable
data class PlaceCreateRequest(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val categoryId: Int? = null,
    val description: String? = null
)

@Serializable
data class PlaceUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val categoryId: Int? = null,
    val description: String? = null
)

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)

fun Place.toDto(
    categoryName: String? = null,
    averageRating: Double = 0.0,
    reviewsCount: Int = 0,
    photosCount: Int = 0,
    baseUrl: String = ""
) = PlaceDto(
    id = id.toString(),
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    categoryId = categoryId,
    categoryName = categoryName,
    description = description,
    coverUrl = coverPath?.let { if (baseUrl.isNotEmpty()) "$baseUrl/files/images/$it" else it },
    uploadedBy = uploadedBy?.toString(),
    status = status.name,
    rejectionReason = rejectionReason,
    averageRating = averageRating,
    reviewsCount = reviewsCount,
    photosCount = photosCount,
    createdAt = createdAt.toString()
)
