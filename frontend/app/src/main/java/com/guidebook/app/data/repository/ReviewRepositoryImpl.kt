package com.guidebook.app.data.repository

import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.ReviewApi
import com.guidebook.app.data.remote.dto.ReviewCreateRequest
import com.guidebook.app.data.remote.dto.ReviewUpdateRequest
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.Review
import com.guidebook.app.domain.repository.ReviewRepository
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val reviewApi: ReviewApi
) : ReviewRepository {

    override suspend fun getReviews(placeId: String): ApiResult<List<Review>> {
        return when (val result = safeApiCall { reviewApi.getReviews(placeId) }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun createReview(placeId: String, rating: Int, comment: String?): ApiResult<Review> {
        return when (val result = safeApiCall {
            reviewApi.createReview(placeId, ReviewCreateRequest(rating, comment))
        }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun updateReview(reviewId: String, rating: Int?, comment: String?): ApiResult<Review> {
        return when (val result = safeApiCall {
            reviewApi.updateReview(reviewId, ReviewUpdateRequest(rating, comment))
        }) {
            is ApiResult.Success      -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error        -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun deleteReview(reviewId: String): ApiResult<Unit> {
        return safeApiCall { reviewApi.deleteReview(reviewId) }
    }
}
