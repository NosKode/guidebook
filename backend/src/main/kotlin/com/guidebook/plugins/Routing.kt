package com.guidebook.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("message" to "Guidebook API v1.0"))
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}
