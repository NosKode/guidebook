package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    /** Живой поток из Room-кеша (офлайн-доступ). */
    fun favoritesFlow(): Flow<List<Place>>

    /** Загружает избранное с сервера и обновляет Room-кеш. */
    suspend fun getFavorites(): ApiResult<List<Place>>

    suspend fun addFavorite(placeId: String): ApiResult<Unit>
    suspend fun removeFavorite(placeId: String): ApiResult<Unit>

    /** Быстрая локальная проверка по Room (без сетевого запроса). */
    suspend fun isFavorite(placeId: String): Boolean
}
