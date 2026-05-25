package com.guidebook.routes

import com.guidebook.service.AuthService
import com.guidebook.service.FavoriteService
import com.guidebook.util.currentUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.favoriteRoutes(favoriteService: FavoriteService, authService: AuthService) {
    route("/api/users/me/favorites") {
        authenticate("auth-jwt") {
            get {
                val user = call.currentUser(authService)
                call.respond(favoriteService.getFavorites(user))
            }
            post("/{placeId}") {
                val user    = call.currentUser(authService)
                val placeId = call.favPlaceUuidOrThrow()
                val added   = favoriteService.addFavorite(user, placeId)
                call.respond(if (added) HttpStatusCode.Created else HttpStatusCode.OK)
            }
            delete("/{placeId}") {
                val user    = call.currentUser(authService)
                val placeId = call.favPlaceUuidOrThrow()
                favoriteService.removeFavorite(user, placeId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.favPlaceUuidOrThrow(): UUID =
    parameters["placeId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw com.guidebook.domain.exception.BadRequestException("Invalid place id")
