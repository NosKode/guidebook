package com.guidebook.routes

import com.guidebook.data.dto.LoginRequest
import com.guidebook.data.dto.RegisterRequest
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.service.AuthService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.payload.subject)
                val user = authService.getUserById(userId)
                call.respond(HttpStatusCode.OK, authService.userToDto(user))
            }

            post("/avatar") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.payload.subject)
                val multipart = call.receiveMultipart()
                var dto = null as com.guidebook.data.dto.UserDto?
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "avatar" && dto == null) {
                        dto = authService.uploadAvatar(userId, part)
                    }
                    part.dispose()
                }
                call.respond(
                    HttpStatusCode.OK,
                    dto ?: throw BadRequestException("No file with name 'avatar' provided")
                )
            }
        }
    }
}
