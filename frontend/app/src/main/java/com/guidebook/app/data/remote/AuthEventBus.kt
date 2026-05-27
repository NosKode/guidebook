package com.guidebook.app.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Глобальная шина событий авторизации.
 * UnauthorizedInterceptor эмитит событие при HTTP 401,
 * NavGraph слушает и перекидывает пользователя на LoginScreen.
 */
object AuthEventBus {

    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Поток событий «сессия истекла» — подписывается NavGraph. */
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    /** Вызывается из OkHttp-потока (не корутина) → tryEmit достаточно. */
    fun emitUnauthorized() {
        _unauthorizedEvent.tryEmit(Unit)
    }
}
