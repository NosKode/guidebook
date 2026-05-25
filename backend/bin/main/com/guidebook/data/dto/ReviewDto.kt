package com.guidebook.data.dto

import com.guidebook.domain.model.Review
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

@Serializable
data class ReviewCreateRequest(
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class ReviewUpdateRequest(
    val rating: Int? = null,
    val comment: String? = null
)

fun Review.toDto(userName: String?) = ReviewDto(
    id = id.toString(),
    placeId = placeId.toString(),
    userId = userId.toString(),
    userName = userName,
    rating = rating,
    comment = comment,
    createdAt = createdAt.toString()
)
