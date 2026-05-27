package com.guidebook.app.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.coroutineContext

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResult.Success(response.body()!!)
        } else {
            ApiResult.Error(response.code(), response.errorBody()?.string() ?: "Unknown error")
        }
    } catch (e: CancellationException) {
        // Корутина была отменена — пробрасываем дальше, не пишем ошибку в UI
        throw e
    } catch (e: IOException) {
        // Перед тем как вернуть NetworkError, убеждаемся что корутина не отменена.
        // OkHttp бросает IOException("Canceled") при отмене вызова — это не сетевая ошибка.
        coroutineContext.ensureActive()
        ApiResult.NetworkError
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        ApiResult.Error(-1, e.message ?: "Unknown error")
    }
}
