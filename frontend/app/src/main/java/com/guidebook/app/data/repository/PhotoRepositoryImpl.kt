package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.PhotoApi
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Photo
import com.guidebook.app.domain.repository.PhotoRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val photoApi: PhotoApi
) : PhotoRepository {

    override suspend fun getPhotos(placeId: String): ApiResult<List<Photo>> {
        return when (val result = safeApiCall { photoApi.getPhotos(placeId) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun uploadPhoto(placeId: String, file: File, caption: String?): ApiResult<Photo> {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", file.name, requestBody)
        val captionBody = caption?.toRequestBody("text/plain".toMediaTypeOrNull())
        return when (val result = safeApiCall { photoApi.uploadPhoto(placeId, part, captionBody) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun deletePhoto(photoId: String): ApiResult<Unit> {
        return safeApiCall { photoApi.deletePhoto(photoId) }
    }
}
