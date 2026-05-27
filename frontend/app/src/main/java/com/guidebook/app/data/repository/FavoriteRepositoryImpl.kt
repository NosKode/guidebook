package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.FavoriteApi
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi
) : FavoriteRepository {

    override suspend fun getFavorites(): ApiResult<List<Place>> {
        return when (val result = safeApiCall { favoriteApi.getFavorites() }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun addFavorite(placeId: String): ApiResult<Unit> {
        return safeApiCall { favoriteApi.addFavorite(placeId) }
    }

    override suspend fun removeFavorite(placeId: String): ApiResult<Unit> {
        return safeApiCall { favoriteApi.removeFavorite(placeId) }
    }

    override suspend fun isFavorite(placeId: String): Boolean {
        return when (val result = getFavorites()) {
            is ApiResult.Success -> result.data.any { it.id == placeId }
            else                 -> false
        }
    }
}
