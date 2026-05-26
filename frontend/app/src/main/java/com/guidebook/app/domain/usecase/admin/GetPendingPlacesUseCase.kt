package com.guidebook.app.domain.usecase.admin

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.AdminRepository
import javax.inject.Inject

class GetPendingPlacesUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): ApiResult<List<Place>> =
        repository.getPendingPlaces()
}
