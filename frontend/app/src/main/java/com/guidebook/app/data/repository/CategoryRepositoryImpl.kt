package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.CategoryApi
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Category
import com.guidebook.app.domain.repository.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryApi: CategoryApi
) : CategoryRepository {

    override suspend fun getCategories(): ApiResult<List<Category>> {
        return when (val result = safeApiCall { categoryApi.getCategories() }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }
}
