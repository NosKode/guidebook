package com.guidebook.app.data.repository

import com.guidebook.app.data.local.db.FavoriteDao
import com.guidebook.app.data.local.db.FavoriteEntity
import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.FavoriteApi
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.PlaceStatus
import com.guidebook.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    // ── Flow из Room — сразу отдаёт кеш, обновляется автоматически ──────────

    override fun favoritesFlow(): Flow<List<Place>> =
        favoriteDao.getAll().map { entities -> entities.map { it.toDomain() } }

    // ── Сетевая синхронизация ────────────────────────────────────────────────

    override suspend fun getFavorites(): ApiResult<List<Place>> {
        return when (val result = safeApiCall { favoriteApi.getFavorites() }) {
            is ApiResult.Success -> {
                val places = result.data.map { it.toDomain() }
                // Полная замена кеша актуальными данными с сервера
                favoriteDao.deleteAll()
                favoriteDao.insert(*places.map { it.toEntity() }.toTypedArray())
                ApiResult.Success(places)
            }
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun addFavorite(placeId: String): ApiResult<Unit> {
        val result = safeApiCall { favoriteApi.addFavorite(placeId) }
        if (result is ApiResult.Success) {
            // Синхронизируем кеш — получаем свежий список с полными данными
            getFavorites()
        }
        return result
    }

    override suspend fun removeFavorite(placeId: String): ApiResult<Unit> {
        val result = safeApiCall { favoriteApi.removeFavorite(placeId) }
        if (result is ApiResult.Success) {
            favoriteDao.deleteById(placeId)
        }
        return result
    }

    // ── Локальная проверка по Room (без сети) ────────────────────────────────

    override suspend fun isFavorite(placeId: String): Boolean =
        favoriteDao.countById(placeId) > 0
}

// ── Маппер Place → FavoriteEntity ───────────────────────────────────────────

private fun Place.toEntity() = FavoriteEntity(
    id            = id,
    name          = name,
    coverUrl      = coverUrl,
    address       = address,
    averageRating = averageRating,
    cachedAt      = System.currentTimeMillis()
)

// ── Маппер FavoriteEntity → Place ───────────────────────────────────────────

private fun FavoriteEntity.toDomain() = Place(
    id             = id,
    name           = name,
    address        = address,
    latitude       = null,
    longitude      = null,
    categoryId     = null,
    categoryName   = null,
    description    = null,
    coverUrl       = coverUrl,
    uploadedBy     = null,
    status         = PlaceStatus.APPROVED,
    rejectionReason = null,
    averageRating  = averageRating,
    reviewsCount   = 0,
    photosCount    = 0,
    createdAt      = ""
)
