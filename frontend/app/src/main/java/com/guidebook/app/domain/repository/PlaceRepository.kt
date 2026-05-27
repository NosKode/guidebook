package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import java.io.File

interface PlaceRepository {
    suspend fun getPlaces(page: Int, pageSize: Int, categoryId: Int? = null, search: String? = null, sortBy: String? = null): ApiResult<PagedData<Place>>
    suspend fun getPlaceById(id: String): ApiResult<Place>
    suspend fun searchPlaces(query: String, page: Int, pageSize: Int): ApiResult<PagedData<Place>>
    suspend fun getMyPlaces(): ApiResult<List<Place>>
    suspend fun addPlace(name: String, address: String?, latitude: Double?, longitude: Double?, categoryId: Int?, description: String?): ApiResult<Place>
    suspend fun uploadCover(placeId: String, file: File): ApiResult<Place>
    suspend fun deletePlace(placeId: String): ApiResult<Unit>
}
