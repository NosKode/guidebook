package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.ReviewCreateRequest
import com.guidebook.app.data.remote.dto.ReviewDto
import com.guidebook.app.data.remote.dto.ReviewUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReviewApi {

    @GET("api/places/{id}/reviews")
    suspend fun getReviews(@Path("id") id: String): Response<List<ReviewDto>>

    @POST("api/places/{id}/reviews")
    suspend fun createReview(
        @Path("id") id: String,
        @Body request: ReviewCreateRequest
    ): Response<ReviewDto>

    @PUT("api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body request: ReviewUpdateRequest
    ): Response<ReviewDto>

    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>
}
