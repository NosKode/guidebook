package com.guidebook.app.domain.model

data class AuthToken(
    val token: String,
    val user: User
)
