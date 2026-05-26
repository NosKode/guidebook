package com.guidebook.app.data.remote.dto

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

data class PlaceCreateRequest(
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val categoryId: Int?,
    val description: String?
)
