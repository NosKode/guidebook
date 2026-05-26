package com.guidebook.app.data.remote.dto

data class PhotoDto(
    val id: String,
    val placeId: String,
    val photoUrl: String,
    val caption: String?,
    val createdAt: String
)
