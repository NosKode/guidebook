package com.guidebook.app.domain.repository

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.AuthToken
import com.guidebook.app.domain.model.User
import java.io.File

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<AuthToken>
    suspend fun register(email: String, password: String, displayName: String?): ApiResult<AuthToken>
    suspend fun getMe(): ApiResult<User>
    suspend fun logout()
    suspend fun uploadAvatar(file: File): ApiResult<User>
}
