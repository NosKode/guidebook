package com.guidebook.app.data.remote.api

import com.guidebook.app.data.remote.dto.AuthResponse
import com.guidebook.app.data.remote.dto.LoginRequest
import com.guidebook.app.data.remote.dto.RegisterRequest
import com.guidebook.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserDto>
}
