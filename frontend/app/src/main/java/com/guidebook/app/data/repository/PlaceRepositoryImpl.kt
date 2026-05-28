package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.PlaceApi
import com.guidebook.app.data.remote.dto.PlaceCreateRequest
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placeApi: PlaceApi
) : PlaceRepository {

    override suspend fun getPlaces(
        page: Int,
        pageSize: Int,
        categoryId: Int?,
        search: String?,
        sortBy: String?
    ): ApiResult<PagedData<Place>> {
        return when (val result = safeApiCall {
            placeApi.getPlaces(
                search = search?.takeIf { it.isNotBlank() },
                category = categoryId,
                page = page,
                pageSize = pageSize,
                sortBy = sortBy
            )
        }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun getPlaceById(id: String): ApiResult<Place> {
        return when (val result = safeApiCall { placeApi.getPlaceById(id) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun searchPlaces(
        query: String,
        page: Int,
        pageSize: Int
    ): ApiResult<PagedData<Place>> = getPlaces(page, pageSize, search = query)

    override suspend fun getMyPlaces(): ApiResult<List<Place>> {
        return when (val result = safeApiCall { placeApi.getMyPlaces() }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun addPlace(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?
    ): ApiResult<Place> {
        return when (val result = safeApiCall {
            placeApi.createPlace(PlaceCreateRequest(name, address, latitude, longitude, categoryId, description))
        }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun uploadCover(placeId: String, file: File): ApiResult<Place> {
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension.lowercase()) ?: "image/jpeg"
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return when (val result = safeApiCall { placeApi.uploadCover(placeId, part) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun deletePlace(placeId: String): ApiResult<Unit> {
        return safeApiCall { placeApi.deletePlace(placeId) }
    }
}
