package com.guidebook.util

import com.guidebook.domain.exception.UnauthorizedException
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import com.guidebook.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.UUID

fun ApplicationCall.userIdOrThrow(): UUID {
    val subject = principal<JWTPrincipal>()?.payload?.subject
        ?: throw UnauthorizedException("Missing or invalid token")
    return runCatching { UUID.fromString(subject) }
        .getOrElse { throw UnauthorizedException("Invalid token subject") }
}

fun ApplicationCall.userRoleOrThrow(): UserRole {
    val roleName = principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
        ?: throw UnauthorizedException("Missing role claim")
    return runCatching { UserRole.valueOf(roleName) }
        .getOrElse { throw UnauthorizedException("Invalid role claim") }
}

suspend fun ApplicationCall.currentUser(authService: AuthService): User =
    authService.getUserById(userIdOrThrow())
