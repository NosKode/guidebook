package com.guidebook.data.dto

import com.guidebook.domain.model.Photo
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    val id: String,
    val placeId: String,
    val photoUrl: String,
    val caption: String?,
    val createdAt: String
)

@Serializable
data class PhotoCreateRequest(
    val caption: String? = null
)

fun Photo.toDto(baseUrl: String) = PhotoDto(
    id = id.toString(),
    placeId = placeId.toString(),
    photoUrl = "$baseUrl/files/images/$filePath",
    caption = caption,
    createdAt = createdAt.toString()
)
