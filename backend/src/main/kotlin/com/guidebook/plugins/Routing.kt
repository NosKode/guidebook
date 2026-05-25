package com.guidebook.plugins

import com.guidebook.routes.adminRoutes
import com.guidebook.routes.authRoutes
import com.guidebook.routes.categoryRoutes
import com.guidebook.routes.favoriteRoutes
import com.guidebook.routes.photoRoutes
import com.guidebook.routes.placeRoutes
import com.guidebook.routes.reviewRoutes
import com.guidebook.service.AuthService
import com.guidebook.service.CategoryService
import com.guidebook.service.FavoriteService
import com.guidebook.service.ModerationService
import com.guidebook.service.PhotoService
import com.guidebook.service.PlaceService
import com.guidebook.service.ReviewService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import java.io.File

fun Application.configureRouting() {
    val authService     = get<AuthService>()
    val categoryService = get<CategoryService>()
    val placeService    = get<PlaceService>()
    val photoService    = get<PhotoService>()
    val reviewService      = get<ReviewService>()
    val favoriteService    = get<FavoriteService>()
    val moderationService  = get<ModerationService>()
    val storagePath     = environment.config.propertyOrNull("storage.path")?.getString() ?: "./storage"

    routing {
        get("/") {
            call.respond(mapOf("message" to "Guidebook API v1.0"))
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        staticFiles("/files/images", File("$storagePath/images"))
        authRoutes(authService)
        categoryRoutes(categoryService, authService)
        placeRoutes(placeService, authService)
        photoRoutes(photoService, authService)
        reviewRoutes(reviewService, authService)
        favoriteRoutes(favoriteService, authService)
        adminRoutes(moderationService, authService)
    }
}
