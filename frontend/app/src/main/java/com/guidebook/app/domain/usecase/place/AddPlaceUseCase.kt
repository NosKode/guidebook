package com.guidebook.app.domain.usecase.place

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import javax.inject.Inject

class AddPlaceUseCase @Inject constructor(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?
    ): ApiResult<Place> =
        repository.addPlace(name, address, latitude, longitude, categoryId, description)
}
