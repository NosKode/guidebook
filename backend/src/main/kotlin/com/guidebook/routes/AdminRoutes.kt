package com.guidebook.routes

import com.guidebook.data.dto.RejectRequest
import com.guidebook.service.AuthService
import com.guidebook.service.ModerationService
import com.guidebook.util.currentUser
import com.guidebook.util.requireAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.adminRoutes(moderationService: ModerationService, authService: AuthService) {
    route("/api/admin/places") {
        authenticate("auth-jwt") {
            get("/pending") {
                val user = call.currentUser(authService)
                requireAdmin(user)
                call.respond(moderationService.listPending())
            }
            post("/{id}/approve") {
                val user    = call.currentUser(authService)
                requireAdmin(user)
                val placeId = call.adminPlaceUuidOrThrow()
                call.respond(moderationService.approve(placeId))
            }
            post("/{id}/reject") {
                val user    = call.currentUser(authService)
                requireAdmin(user)
                val placeId = call.adminPlaceUuidOrThrow()
                val request = call.receive<RejectRequest>()
                call.respond(moderationService.reject(placeId, request.reason))
            }
        }
    }
}

private fun ApplicationCall.adminPlaceUuidOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw com.guidebook.domain.exception.BadRequestException("Invalid place id")
