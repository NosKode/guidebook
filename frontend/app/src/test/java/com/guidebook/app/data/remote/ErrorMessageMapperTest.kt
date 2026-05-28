package com.guidebook.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorMessageMapperTest {

    private fun error(code: Int, message: String = "") = ApiResult.Error(code, message)

    @Test
    fun `401 returns invalid credentials message`() {
        assertEquals("Неверный email или пароль", error(401).friendlyMessage())
    }

    @Test
    fun `403 returns no access message`() {
        assertEquals("Нет доступа", error(403).friendlyMessage())
    }

    @Test
    fun `404 returns not found message`() {
        assertEquals("Не найдено", error(404).friendlyMessage())
    }

    @Test
    fun `409 returns email taken message`() {
        assertEquals("Этот email уже занят", error(409).friendlyMessage())
    }

    @Test
    fun `500 returns server error message`() {
        assertEquals("Ошибка сервера. Попробуйте позже", error(500).friendlyMessage())
    }

    @Test
    fun `503 returns server error message`() {
        assertEquals("Ошибка сервера. Попробуйте позже", error(503).friendlyMessage())
    }

    @Test
    fun `code minus 1 returns unknown error message`() {
        assertEquals("Неизвестная ошибка. Попробуйте позже", error(-1).friendlyMessage())
    }

    @Test
    fun `unrecognised code returns generic message`() {
        assertEquals("Что-то пошло не так. Попробуйте позже", error(418).friendlyMessage())
    }

    @Test
    fun `400 with email invalid in json message returns email format error`() {
        val body = """{"error":"Bad Request","message":"invalid email format"}"""
        assertEquals("Некорректный формат email", error(400, body).friendlyMessage())
    }

    @Test
    fun `400 with password and 8 in json message returns password length error`() {
        val body = """{"error":"Bad Request","message":"password must be at least 8 characters"}"""
        assertEquals("Пароль должен содержать минимум 8 символов", error(400, body).friendlyMessage())
    }

    @Test
    fun `400 with already taken in json message returns email taken error`() {
        val body = """{"error":"Bad Request","message":"Email already taken"}"""
        assertEquals("Этот email уже занят", error(400, body).friendlyMessage())
    }

    @Test
    fun `400 with already registered in json message returns email taken error`() {
        val body = """{"error":"Bad Request","message":"user already registered"}"""
        assertEquals("Этот email уже занят", error(400, body).friendlyMessage())
    }

    @Test
    fun `400 with unrecognised message returns generic validation error`() {
        val body = """{"error":"Bad Request","message":"something unexpected"}"""
        assertEquals("Проверьте правильность введённых данных", error(400, body).friendlyMessage())
    }

    @Test
    fun `400 with raw body no json returns generic validation error`() {
        assertEquals("Проверьте правильность введённых данных", error(400, "Bad Request").friendlyMessage())
    }
}
