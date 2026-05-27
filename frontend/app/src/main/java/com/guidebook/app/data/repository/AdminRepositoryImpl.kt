package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.AdminApi
import com.guidebook.app.data.remote.dto.RejectRequest
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi
) : AdminRepository {

    override suspend fun getPendingPlaces(): ApiResult<List<Place>> {
        return when (val result = safeApiCall { adminApi.getPendingPlaces() }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun approvePlace(placeId: String): ApiResult<Place> {
        return when (val result = safeApiCall { adminApi.approvePlace(placeId) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun rejectPlace(placeId: String, reason: String?): ApiResult<Place> {
        return when (val result = safeApiCall { adminApi.rejectPlace(placeId, RejectRequest(reason)) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }
}
