package com.guidebook.app.domain.usecase.auth

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.AuthToken
import com.guidebook.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String?): ApiResult<AuthToken> =
        repository.register(email, password, displayName)
}
