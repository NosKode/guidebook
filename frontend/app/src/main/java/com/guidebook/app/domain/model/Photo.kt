package com.guidebook.app.domain.model

data class Photo(
    val id: String,
    val placeId: String,
    val photoUrl: String,
    val caption: String?,
    val createdAt: String
)
