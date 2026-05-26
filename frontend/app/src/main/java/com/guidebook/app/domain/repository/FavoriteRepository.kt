package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place

interface FavoriteRepository {
    suspend fun getFavorites(): ApiResult<List<Place>>
    suspend fun addFavorite(placeId: String): ApiResult<Unit>
    suspend fun removeFavorite(placeId: String): ApiResult<Unit>
    suspend fun isFavorite(placeId: String): Boolean
}
