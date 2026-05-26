package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Photo
import java.io.File

interface PhotoRepository {
    suspend fun getPhotos(placeId: String): ApiResult<List<Photo>>
    suspend fun uploadPhoto(placeId: String, file: File, caption: String?): ApiResult<Photo>
    suspend fun deletePhoto(photoId: String): ApiResult<Unit>
}
