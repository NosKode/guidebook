package com.guidebook.service

import com.guidebook.data.dto.PlaceDto
import com.guidebook.data.dto.toDto
import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.FavoriteRepository
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.User
import java.util.UUID

class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val placeRepository: PlaceRepository,
    private val categoryRepository: CategoryRepository,
    private val baseUrl: String,
    private val staticMapsKey: String = ""
) {

    suspend fun getFavorites(user: User): List<PlaceDto> =
        enrich(favoriteRepository.findByUser(user.id))

    suspend fun addFavorite(user: User, placeId: UUID): Boolean {
        placeRepository.findById(placeId) ?: throw NotFoundException("Place not found")
        return favoriteRepository.add(user.id, placeId)
    }

    suspend fun removeFavorite(user: User, placeId: UUID) {
        if (!favoriteRepository.remove(user.id, placeId))
            throw NotFoundException("Favorite not found")
    }

    private suspend fun enrich(places: List<Place>): List<PlaceDto> {
        if (places.isEmpty()) return emptyList()
        val ids = places.map { it.id }
        val reviewStats = placeRepository.getReviewStatsBatch(ids)
        val photoCounts = placeRepository.getPhotosCountBatch(ids)
        val categories  = places.mapNotNull { it.categoryId }.toSet()
            .mapNotNull { catId -> categoryRepository.findById(catId)?.let { catId to it.name } }
            .toMap()
        return places.map { place ->
            val (avg, count) = reviewStats[place.id] ?: Pair(0.0, 0)
            place.toDto(
                categoryName = place.categoryId?.let { categories[it] },
                averageRating = avg,
                reviewsCount = count,
                photosCount = photoCounts[place.id] ?: 0,
                baseUrl = baseUrl,
                staticMapsKey = staticMapsKey
            )
        }
    }
}
