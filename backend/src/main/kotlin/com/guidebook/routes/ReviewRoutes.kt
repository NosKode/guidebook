package com.guidebook.routes

import com.guidebook.data.dto.ReviewCreateRequest
import com.guidebook.data.dto.ReviewUpdateRequest
import com.guidebook.service.AuthService
import com.guidebook.service.ReviewService
import com.guidebook.util.currentUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.reviewRoutes(reviewService: ReviewService, authService: AuthService) {
    route("/api/places/{id}/reviews") {
        get {
            val placeId = call.placeUuidOrThrow()
            call.respond(reviewService.getReviewsForPlace(placeId))
        }
        authenticate("auth-jwt") {
            post {
                val placeId = call.placeUuidOrThrow()
                val user    = call.currentUser(authService)
                val request = call.receive<ReviewCreateRequest>()
                val dto = reviewService.createReview(placeId, user, request)
                call.respond(HttpStatusCode.Created, dto)
            }
        }
    }
    route("/api/reviews/{id}") {
        authenticate("auth-jwt") {
            put {
                val reviewId = call.reviewUuidOrThrow()
                val user     = call.currentUser(authService)
                val request  = call.receive<ReviewUpdateRequest>()
                call.respond(reviewService.updateReview(reviewId, user, request))
            }
            delete {
                val reviewId = call.reviewUuidOrThrow()
                val user     = call.currentUser(authService)
                reviewService.deleteReview(reviewId, user)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.placeUuidOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw com.guidebook.domain.exception.BadRequestException("Invalid place id")

private fun ApplicationCall.reviewUuidOrThrow(): UUID =
    parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw com.guidebook.domain.exception.BadRequestException("Invalid review id")
