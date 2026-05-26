package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.UserDto
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.model.UserRole

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    createdAt = createdAt
)
