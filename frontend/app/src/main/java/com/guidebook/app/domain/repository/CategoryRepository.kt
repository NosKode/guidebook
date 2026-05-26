package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): ApiResult<List<Category>>
}
