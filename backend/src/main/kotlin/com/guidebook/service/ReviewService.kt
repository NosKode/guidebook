package com.guidebook.service

import com.guidebook.data.dto.ReviewCreateRequest
import com.guidebook.data.dto.ReviewDto
import com.guidebook.data.dto.ReviewUpdateRequest
import com.guidebook.data.dto.toDto
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.data.repository.ReviewRepository
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import java.util.UUID

class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val placeRepository: PlaceRepository,
    private val baseUrl: String
) {

    suspend fun getReviewsForPlace(placeId: UUID): List<ReviewDto> {
        placeRepository.findById(placeId) ?: throw NotFoundException("Place not found")
        return reviewRepository.findByPlace(placeId).map {
            it.review.toDto(
                userName = it.userName,
                userAvatarUrl = it.userAvatarPath?.let { p -> "$baseUrl/files/images/$p" }
            )
        }
    }

    suspend fun createReview(placeId: UUID, user: User, request: ReviewCreateRequest): ReviewDto {
        if (request.rating !in 1..5) throw BadRequestException("Rating must be between 1 and 5")
        placeRepository.findById(placeId) ?: throw NotFoundException("Place not found")
        if (reviewRepository.findByUserAndPlace(user.id, placeId) != null)
            throw ConflictException("Already reviewed, use PUT to update")
        val review = reviewRepository.create(placeId, user.id, request.rating, request.comment?.trim())
        return review.toDto(
            userName = user.displayName,
            userAvatarUrl = user.avatarPath?.let { "$baseUrl/files/images/$it" }
        )
    }

    suspend fun updateReview(reviewId: UUID, user: User, request: ReviewUpdateRequest): ReviewDto {
        val existing = reviewRepository.findById(reviewId) ?: throw NotFoundException("Review not found")
        if (existing.userId != user.id) throw ForbiddenException("Only the author can edit this review")
        request.rating?.let { if (it !in 1..5) throw BadRequestException("Rating must be between 1 and 5") }
        val updated = reviewRepository.update(reviewId, request.rating, request.comment?.trim())
            ?: throw NotFoundException("Review not found")
        return updated.toDto(
            userName = user.displayName,
            userAvatarUrl = user.avatarPath?.let { "$baseUrl/files/images/$it" }
        )
    }

    suspend fun deleteReview(reviewId: UUID, user: User) {
        val existing = reviewRepository.findById(reviewId) ?: throw NotFoundException("Review not found")
        if (user.role != UserRole.ADMIN && existing.userId != user.id)
            throw ForbiddenException("Only the author or admin can delete this review")
        reviewRepository.delete(reviewId)
    }
}
