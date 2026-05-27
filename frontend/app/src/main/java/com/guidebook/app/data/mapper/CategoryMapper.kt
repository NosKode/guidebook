package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.CategoryDto
import com.guidebook.app.domain.model.Category

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description
)
