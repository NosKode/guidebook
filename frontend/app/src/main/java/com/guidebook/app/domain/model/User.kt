package com.guidebook.app.domain.model

enum class UserRole { USER, ADMIN }

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val createdAt: String
)
