package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Review

interface ReviewRepository {
    suspend fun getReviews(placeId: String): ApiResult<List<Review>>
    suspend fun createReview(placeId: String, rating: Int, comment: String?): ApiResult<Review>
    suspend fun updateReview(reviewId: String, rating: Int?, comment: String?): ApiResult<Review>
    suspend fun deleteReview(reviewId: String): ApiResult<Unit>
}
