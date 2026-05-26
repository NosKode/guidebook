package com.guidebook.app.domain.usecase.place

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import javax.inject.Inject

class GetMyPlacesUseCase @Inject constructor(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(): ApiResult<List<Place>> =
        repository.getMyPlaces()
}
