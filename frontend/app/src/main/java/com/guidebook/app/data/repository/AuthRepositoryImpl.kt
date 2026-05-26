package com.guidebook.app.data.repository

import com.guidebook.app.data.local.prefs.TokenStorage
import com.guidebook.app.data.mapper.toDomain
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.data.remote.api.AuthApi
import com.guidebook.app.data.remote.dto.LoginRequest
import com.guidebook.app.data.remote.dto.RegisterRequest
import com.guidebook.app.data.remote.safeApiCall
import com.guidebook.app.domain.model.AuthToken
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<AuthToken> {
        return when (val result = safeApiCall { authApi.login(LoginRequest(email, password)) }) {
            is ApiResult.Success -> {
                tokenStorage.saveToken(result.data.token)
                ApiResult.Success(AuthToken(result.data.token, result.data.user.toDomain()))
            }
            is ApiResult.Error   -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun register(email: String, password: String, displayName: String?): ApiResult<AuthToken> {
        return when (val result = safeApiCall { authApi.register(RegisterRequest(email, password, displayName)) }) {
            is ApiResult.Success -> {
                tokenStorage.saveToken(result.data.token)
                ApiResult.Success(AuthToken(result.data.token, result.data.user.toDomain()))
            }
            is ApiResult.Error   -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun getMe(): ApiResult<User> {
        return when (val result = safeApiCall { authApi.getMe() }) {
            is ApiResult.Success  -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error    -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun logout() {
        tokenStorage.clearToken()
    }
}
