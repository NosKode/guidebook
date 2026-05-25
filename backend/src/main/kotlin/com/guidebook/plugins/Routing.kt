package com.guidebook.plugins

import com.guidebook.routes.authRoutes
import com.guidebook.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    val authService = get<AuthService>()

    routing {
        get("/") {
            call.respond(mapOf("message" to "Guidebook API v1.0"))
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        authRoutes(authService)
    }
}
