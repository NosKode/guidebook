package com.guidebook.service

import com.guidebook.data.dto.PagedResponse
import com.guidebook.data.dto.PlaceCreateRequest
import com.guidebook.data.dto.PlaceDto
import com.guidebook.data.dto.PlaceUpdateRequest
import com.guidebook.data.dto.toDto
import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import io.ktor.http.content.*
import java.util.UUID
import kotlin.math.ceil

class PlaceService(
    private val placeRepository: PlaceRepository,
    private val categoryRepository: CategoryRepository,
    private val fileStorageService: FileStorageService,
    private val baseUrl: String,
    private val staticMapsKey: String = ""
) {

    suspend fun getApprovedPlaces(
        categoryId: Int?, search: String?, page: Int, pageSize: Int, sortBy: String? = null
    ): PagedResponse<PlaceDto> {
        val result = placeRepository.findApproved(categoryId, search, page, pageSize, sortBy)
        val totalPages = if (result.totalItems == 0L) 0
            else ceil(result.totalItems.toDouble() / pageSize).toInt()
        return PagedResponse(
            items = enrich(result.items),
            page = page,
            pageSize = pageSize,
            totalItems = result.totalItems,
            totalPages = totalPages
        )
    }

    suspend fun getPlace(id: UUID, caller: User?): PlaceDto {
        val place = placeRepository.findById(id) ?: throw NotFoundException("Place not found")
        if (place.status != PlaceStatus.APPROVED) {
            if (caller == null || (caller.role != UserRole.ADMIN && caller.id != place.uploadedBy))
                throw NotFoundException("Place not found")
        }
        return enrichSingle(place)
    }

    suspend fun getMyPlaces(user: User): List<PlaceDto> =
        enrich(placeRepository.findByUploader(user.id))

    suspend fun getPendingPlaces(user: User): List<PlaceDto> {
        requireAdmin(user)
        return enrich(placeRepository.findPending())
    }

    suspend fun createPlace(user: User, request: PlaceCreateRequest): PlaceDto {
        val status = if (user.role == UserRole.ADMIN) PlaceStatus.APPROVED else PlaceStatus.PENDING
        val place = placeRepository.create(
            name = request.name.trim(),
            address = request.address?.trim(),
            latitude = request.latitude,
            longitude = request.longitude,
            categoryId = request.categoryId,
            description = request.description?.trim(),
            uploadedBy = user.id,
            status = status
        )
        return enrichSingle(place)
    }

    suspend fun updatePlace(id: UUID, user: User, request: PlaceUpdateRequest): PlaceDto {
        val existing = placeRepository.findById(id) ?: throw NotFoundException("Place not found")
        checkOwnerOrAdmin(user, existing)
        val place = placeRepository.update(
            id = id,
            name = request.name?.trim(),
            address = request.address?.trim(),
            latitude = request.latitude,
            longitude = request.longitude,
            categoryId = request.categoryId,
            description = request.description?.trim()
        ) ?: throw NotFoundException("Place not found")
        return enrichSingle(place)
    }

    suspend fun deletePlace(id: UUID, user: User) {
        val existing = placeRepository.findById(id) ?: throw NotFoundException("Place not found")
        checkOwnerOrAdmin(user, existing)
        placeRepository.delete(id)
    }

    suspend fun uploadCover(id: UUID, user: User, part: PartData.FileItem): PlaceDto {
        val existing = placeRepository.findById(id) ?: throw NotFoundException("Place not found")
        checkOwnerOrAdmin(user, existing)
        existing.coverPath?.let { fileStorageService.deleteFile(it) }
        val relativePath = fileStorageService.saveCover(id, part)
        placeRepository.updateCoverPath(id, relativePath)
        return enrichSingle(placeRepository.findById(id)!!)
    }

    suspend fun calculateAverageRating(placeId: UUID): Double =
        placeRepository.getReviewStatsBatch(listOf(placeId))[placeId]?.first ?: 0.0

    private fun checkOwnerOrAdmin(user: User, place: Place) {
        if (user.role != UserRole.ADMIN && user.id != place.uploadedBy)
            throw ForbiddenException("You do not have permission to modify this place")
    }

    private fun requireAdmin(user: User) {
        if (user.role != UserRole.ADMIN)
            throw ForbiddenException("Admin access required")
    }

    private suspend fun enrich(places: List<Place>): List<PlaceDto> {
        if (places.isEmpty()) return emptyList()
        val ids = places.map { it.id }
        val reviewStats = placeRepository.getReviewStatsBatch(ids)
        val photoCounts = placeRepository.getPhotosCountBatch(ids)
        val categories = places.mapNotNull { it.categoryId }.toSet()
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

    private suspend fun enrichSingle(place: Place): PlaceDto {
        val stats = placeRepository.getReviewStatsBatch(listOf(place.id))
        val photos = placeRepository.getPhotosCountBatch(listOf(place.id))
        val categoryName = place.categoryId?.let { categoryRepository.findById(it)?.name }
        val (avg, count) = stats[place.id] ?: Pair(0.0, 0)
        return place.toDto(
            categoryName = categoryName,
            averageRating = avg,
            reviewsCount = count,
            photosCount = photos[place.id] ?: 0,
            baseUrl = baseUrl,
            staticMapsKey = staticMapsKey
        )
    }
}
