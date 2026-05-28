package com.guidebook.data.dto

import com.guidebook.domain.model.Review
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String?,
    val userAvatarUrl: String?,
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

fun Review.toDto(userName: String?, userAvatarUrl: String?) = ReviewDto(
    id = id.toString(),
    placeId = placeId.toString(),
    userId = userId.toString(),
    userName = userName,
    userAvatarUrl = userAvatarUrl,
    rating = rating,
    comment = comment,
    createdAt = createdAt.toString()
)
