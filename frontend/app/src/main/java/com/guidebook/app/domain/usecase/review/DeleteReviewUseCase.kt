package com.guidebook.app.domain.usecase.review

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.repository.ReviewRepository
import javax.inject.Inject

class DeleteReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(reviewId: String): ApiResult<Unit> =
        repository.deleteReview(reviewId)
}
