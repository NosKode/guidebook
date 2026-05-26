package com.guidebook.app.domain.usecase.photo

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Photo
import com.guidebook.app.domain.repository.PhotoRepository
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(placeId: String): ApiResult<List<Photo>> =
        repository.getPhotos(placeId)
}
