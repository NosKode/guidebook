package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.AuthResponse
import com.guidebook.app.data.remote.dto.LoginRequest
import com.guidebook.app.data.remote.dto.RegisterRequest
import com.guidebook.app.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserDto>

    @Multipart
    @POST("api/auth/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<UserDto>
}
