package com.guidebook.routes

import com.guidebook.data.dto.PlaceCreateRequest
import com.guidebook.data.dto.PlaceDto
import com.guidebook.data.dto.PlaceUpdateRequest
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.service.AuthService
import com.guidebook.service.PlaceService
import com.guidebook.util.currentUser
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.placeRoutes(placeService: PlaceService, authService: AuthService) {
    route("/api/places") {

        get {
            val categoryId = call.request.queryParameters["category"]?.toIntOrNull()
            val search     = call.request.queryParameters["search"]
            val sortBy     = call.request.queryParameters["sortBy"]
            val page       = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize   = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
            call.respond(HttpStatusCode.OK, placeService.getApprovedPlaces(categoryId, search, page, pageSize, sortBy))
        }

        authenticate("auth-jwt") {
            get("/mine") {
                val user = call.currentUser(authService)
                call.respond(HttpStatusCode.OK, placeService.getMyPlaces(user))
            }

            post {
                val user = call.currentUser(authService)
                val request = call.receive<PlaceCreateRequest>()
                call.respond(HttpStatusCode.Created, placeService.createPlace(user, request))
            }

            put("/{id}") {
                val user = call.currentUser(authService)
                val id   = call.placeIdOrThrow()
                call.respond(HttpStatusCode.OK, placeService.updatePlace(id, user, call.receive<PlaceUpdateRequest>()))
            }

            delete("/{id}") {
                val user = call.currentUser(authService)
                val id   = call.placeIdOrThrow()
                placeService.deletePlace(id, user)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/{id}/cover") {
                val user = call.currentUser(authService)
                val id   = call.placeIdOrThrow()
                val multipart = call.receiveMultipart()
                var dto: PlaceDto? = null
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "cover" && dto == null) {
                        dto = placeService.uploadCover(id, user, part)
                    }
                }
                call.respond(HttpStatusCode.OK, dto ?: throw BadRequestException("No file with name 'cover' provided"))
            }
        }

        authenticate("auth-jwt", optional = true) {
            get("/{id}") {
                val id = call.placeIdOrThrow()
                val caller = call.principal<JWTPrincipal>()?.let { principal ->
                    runCatching {
                        val userId = UUID.fromString(principal.payload.subject)
                        authService.getUserById(userId)
                    }.getOrNull()
                }
                call.respond(HttpStatusCode.OK, placeService.getPlace(id, caller))
            }
        }
    }
}

private fun ApplicationCall.placeIdOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw BadRequestException("Invalid place id")
