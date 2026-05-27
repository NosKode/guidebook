package com.guidebook.app.data.remote.interceptor

import com.guidebook.app.data.local.prefs.TokenStorage
import com.guidebook.app.data.remote.AuthEventBus
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp-interceptor: перехватывает 401 Unauthorized.
 * При получении 401 — очищает сохранённый токен и отправляет
 * событие в [AuthEventBus], чтобы NavGraph перенаправил пользователя на Login.
 */
class UnauthorizedInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            runBlocking { tokenStorage.clearToken() }
            AuthEventBus.emitUnauthorized()
        }
        return response
    }
}
