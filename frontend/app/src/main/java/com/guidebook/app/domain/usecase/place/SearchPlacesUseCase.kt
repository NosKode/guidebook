package com.guidebook.app.domain.usecase.place

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1, pageSize: Int = 20): ApiResult<PagedData<Place>> =
        repository.searchPlaces(query, page, pageSize)
}
