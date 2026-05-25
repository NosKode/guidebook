package com.guidebook.routes

import com.guidebook.domain.exception.BadRequestException
import com.guidebook.service.AuthService
import com.guidebook.service.PhotoService
import com.guidebook.util.currentUser
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.photoRoutes(photoService: PhotoService, authService: AuthService) {

    route("/api/places/{id}/photos") {

        get {
            val placeId = call.placeUuidOrThrow()
            call.respond(HttpStatusCode.OK, photoService.getPhotosForPlace(placeId))
        }

        authenticate("auth-jwt") {
            post {
                val user    = call.currentUser(authService)
                val placeId = call.placeUuidOrThrow()
                val multipart = call.receiveMultipart()
                var caption: String? = null
                var dto: com.guidebook.data.dto.PhotoDto? = null
                multipart.forEachPart { part ->
                    when {
                        part is PartData.FormItem && part.name == "caption" ->
                            caption = part.value
                        part is PartData.FileItem && part.name == "photo" && dto == null ->
                            dto = photoService.uploadPhoto(placeId, user, caption, part)
                    }
                }
                call.respond(
                    HttpStatusCode.Created,
                    dto ?: throw BadRequestException("No file with name 'photo' provided")
                )
            }
        }
    }

    route("/api/photos") {
        authenticate("auth-jwt") {
            delete("/{id}") {
                val user    = call.currentUser(authService)
                val photoId = call.photoUuidOrThrow()
                photoService.deletePhoto(photoId, user)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.placeUuidOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw BadRequestException("Invalid place id")

private fun ApplicationCall.photoUuidOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw BadRequestException("Invalid photo id")
