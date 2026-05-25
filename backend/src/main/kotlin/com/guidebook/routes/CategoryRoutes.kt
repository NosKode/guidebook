package com.guidebook.routes

import com.guidebook.data.dto.CategoryCreateRequest
import com.guidebook.data.dto.CategoryUpdateRequest
import com.guidebook.data.dto.toDto
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.service.AuthService
import com.guidebook.service.CategoryService
import com.guidebook.util.currentUser
import com.guidebook.util.requireAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryService: CategoryService, authService: AuthService) {
    route("/api/categories") {

        get {
            val categories = categoryService.findAll()
            call.respond(HttpStatusCode.OK, categories.map { it.toDto() })
        }

        authenticate("auth-jwt") {
            post {
                val user = call.currentUser(authService)
                requireAdmin(user)
                val request = call.receive<CategoryCreateRequest>()
                val category = categoryService.create(request)
                call.respond(HttpStatusCode.Created, category.toDto())
            }

            put("/{id}") {
                val user = call.currentUser(authService)
                requireAdmin(user)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid category id")
                val request = call.receive<CategoryUpdateRequest>()
                val category = categoryService.update(id, request)
                call.respond(HttpStatusCode.OK, category.toDto())
            }

            delete("/{id}") {
                val user = call.currentUser(authService)
                requireAdmin(user)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid category id")
                categoryService.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
