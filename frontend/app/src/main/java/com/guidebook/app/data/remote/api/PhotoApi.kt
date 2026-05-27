package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.PhotoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoApi {

    @GET("api/places/{id}/photos")
    suspend fun getPhotos(@Path("id") id: String): Response<List<PhotoDto>>

    @Multipart
    @POST("api/places/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part,
        @Part("caption") caption: RequestBody? = null
    ): Response<PhotoDto>

    @DELETE("api/photos/{id}")
    suspend fun deletePhoto(@Path("id") id: String): Response<Unit>
}
