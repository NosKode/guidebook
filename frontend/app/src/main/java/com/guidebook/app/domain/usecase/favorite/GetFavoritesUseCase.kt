package com.guidebook.app.domain.usecase.favorite

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(): ApiResult<List<Place>> =
        repository.getFavorites()
}
