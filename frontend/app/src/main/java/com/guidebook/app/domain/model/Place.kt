package com.guidebook.app.domain.model

enum class PlaceStatus { PENDING, APPROVED, REJECTED }

data class Place(
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
    val status: PlaceStatus,
    val rejectionReason: String?,
    val averageRating: Double,
    val reviewsCount: Int,
    val photosCount: Int,
    val createdAt: String
)
