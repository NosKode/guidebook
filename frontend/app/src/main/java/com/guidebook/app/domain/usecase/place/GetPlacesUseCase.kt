package com.guidebook.app.domain.usecase.place

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.PlaceRepository
import javax.inject.Inject

class GetPlacesUseCase @Inject constructor(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
        categoryId: Int? = null,
        search: String? = null
    ): ApiResult<PagedData<Place>> =
        repository.getPlaces(page, pageSize, categoryId, search)
}
