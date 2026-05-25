package com.guidebook.plugins

import com.guidebook.domain.exception.AppException
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.exception.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            val status = when (cause) {
                is NotFoundException     -> HttpStatusCode.NotFound
                is ForbiddenException   -> HttpStatusCode.Forbidden
                is BadRequestException  -> HttpStatusCode.BadRequest
                is ConflictException    -> HttpStatusCode.Conflict
                is UnauthorizedException -> HttpStatusCode.Unauthorized
            }
            call.respond(status, mapOf("error" to status.description, "message" to cause.message))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal Server Error", "message" to "Internal server error")
            )
        }
    }
}
