package com.guidebook.app.domain.usecase.place

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import javax.inject.Inject

class GetPlaceDetailUseCase @Inject constructor(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(id: String): ApiResult<Place> =
        repository.getPlaceById(id)
}
