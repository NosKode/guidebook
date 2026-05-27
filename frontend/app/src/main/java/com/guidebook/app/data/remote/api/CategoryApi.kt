package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.CategoryDto
import retrofit2.Response
import retrofit2.http.GET

interface CategoryApi {

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}
