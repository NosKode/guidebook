package com.guidebook.service

import com.guidebook.data.dto.PhotoDto
import com.guidebook.data.dto.toDto
import com.guidebook.data.repository.PhotoRepository
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import io.ktor.http.content.*
import java.util.UUID

class PhotoService(
    private val photoRepository: PhotoRepository,
    private val placeRepository: PlaceRepository,
    private val fileStorageService: FileStorageService,
    private val baseUrl: String
) {

    suspend fun getPhotosForPlace(placeId: UUID): List<PhotoDto> {
        placeRepository.findById(placeId) ?: throw NotFoundException("Place not found")
        return photoRepository.findByPlace(placeId).map { it.toDto(baseUrl) }
    }

    suspend fun uploadPhoto(placeId: UUID, user: User, caption: String?, part: PartData.FileItem): PhotoDto {
        val place = placeRepository.findById(placeId) ?: throw NotFoundException("Place not found")
        checkOwnerOrAdmin(user, place)
        val photoId = UUID.randomUUID()
        val filePath = fileStorageService.savePhoto(photoId, part)
        val photo = photoRepository.create(placeId, filePath, caption?.trim()?.takeIf { it.isNotBlank() })
        return photo.toDto(baseUrl)
    }

    suspend fun deletePhoto(photoId: UUID, user: User) {
        val photo = photoRepository.findById(photoId) ?: throw NotFoundException("Photo not found")
        val place = placeRepository.findById(photo.placeId) ?: throw NotFoundException("Place not found")
        checkOwnerOrAdmin(user, place)
        fileStorageService.deleteFile(photo.filePath)
        photoRepository.delete(photoId)
    }

    private fun checkOwnerOrAdmin(user: User, place: Place) {
        if (user.role != UserRole.ADMIN && user.id != place.uploadedBy)
            throw ForbiddenException("You do not have permission to modify this place")
    }
}
