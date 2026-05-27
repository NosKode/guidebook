package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.PlaceDto
import com.guidebook.app.data.remote.dto.RejectRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AdminApi {

    @GET("api/admin/places/pending")
    suspend fun getPendingPlaces(): Response<List<PlaceDto>>

    @POST("api/admin/places/{id}/approve")
    suspend fun approvePlace(@Path("id") id: String): Response<PlaceDto>

    @POST("api/admin/places/{id}/reject")
    suspend fun rejectPlace(
        @Path("id") id: String,
        @Body body: RejectRequest
    ): Response<PlaceDto>
}
