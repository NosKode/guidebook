package com.guidebook.app.data.remote.dto

data class ReviewDto(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class ReviewCreateRequest(
    val rating: Int,
    val comment: String?
)

data class ReviewUpdateRequest(
    val rating: Int?,
    val comment: String?
)
