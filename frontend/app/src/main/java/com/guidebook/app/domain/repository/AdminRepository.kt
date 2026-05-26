package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place

interface AdminRepository {
    suspend fun getPendingPlaces(): ApiResult<List<Place>>
    suspend fun approvePlace(placeId: String): ApiResult<Place>
    suspend fun rejectPlace(placeId: String, reason: String): ApiResult<Place>
}
