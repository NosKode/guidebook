package com.guidebook.app.data.remote

/**
 * Переводит сырой API-ответ в читаемое сообщение на русском.
 * Бэкенд отдаёт JSON вида: {"error":"Unauthorized","message":"Invalid credentials"}
 */
fun ApiResult.Error.friendlyMessage(): String {
    return when (code) {
        400 -> {
            val msg = extractJsonMessage(message).lowercase()
            when {
                "email" in msg && ("invalid" in msg || "format" in msg) ->
                    "Некорректный формат email"
                "password" in msg && "8" in msg ->
                    "Пароль должен содержать минимум 8 символов"
                "password" in msg ->
                    "Пароль не соответствует требованиям"
                "already taken" in msg || "already registered" in msg ->
                    "Этот email уже занят"
                else ->
                    "Проверьте правильность введённых данных"
            }
        }
        401  -> "Неверный email или пароль"
        403  -> "Нет доступа"
        404  -> "Не найдено"
        409  -> "Этот email уже занят"
        in 500..599 -> "Ошибка сервера. Попробуйте позже"
        -1   -> "Неизвестная ошибка. Попробуйте позже"
        else -> "Что-то пошло не так. Попробуйте позже"
    }
}

/** Извлекает поле "message" из JSON-тела ответа. */
private fun extractJsonMessage(body: String): String =
    Regex(""""message"\s*:\s*"([^"]+)"""").find(body)
        ?.groupValues?.getOrNull(1)
        ?: body
