package com.guidebook.app.domain.usecase.favorite

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(placeId: String): ApiResult<Unit> =
        repository.addFavorite(placeId)
}
