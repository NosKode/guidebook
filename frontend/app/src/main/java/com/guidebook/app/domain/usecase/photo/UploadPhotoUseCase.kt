package com.guidebook.app.domain.usecase.photo

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Photo
import com.guidebook.app.domain.repository.PhotoRepository
import java.io.File
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(placeId: String, file: File, caption: String?): ApiResult<Photo> =
        repository.uploadPhoto(placeId, file, caption)
}
