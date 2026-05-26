package com.guidebook.app.domain.usecase.review

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Review
import com.guidebook.app.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(placeId: String): ApiResult<List<Review>> =
        repository.getReviews(placeId)
}
