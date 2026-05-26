package com.guidebook.app.domain.model

data class Review(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)
