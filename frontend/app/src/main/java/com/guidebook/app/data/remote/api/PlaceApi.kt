package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.PagedResponse
import com.guidebook.app.data.remote.dto.PlaceCreateRequest
import com.guidebook.app.data.remote.dto.PlaceDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaceApi {

    @GET("api/places")
    suspend fun getPlaces(
        @Query("search") search: String? = null,
        @Query("category") category: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String? = null
    ): Response<PagedResponse<PlaceDto>>

    @GET("api/places/{id}")
    suspend fun getPlaceById(@Path("id") id: String): Response<PlaceDto>

    @GET("api/places/mine")
    suspend fun getMyPlaces(): Response<List<PlaceDto>>

    @POST("api/places")
    suspend fun createPlace(@Body request: PlaceCreateRequest): Response<PlaceDto>

    @PUT("api/places/{id}")
    suspend fun updatePlace(
        @Path("id") id: String,
        @Body request: PlaceCreateRequest
    ): Response<PlaceDto>

    @DELETE("api/places/{id}")
    suspend fun deletePlace(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("api/places/{id}/cover")
    suspend fun uploadCover(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<PlaceDto>
}
