package com.guidebook.app.domain.usecase.admin

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.AdminRepository
import javax.inject.Inject

class RejectPlaceUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(placeId: String, reason: String): ApiResult<Place> =
        repository.rejectPlace(placeId, reason)
}
