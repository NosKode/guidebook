package com.guidebook.plugins

import com.guidebook.config.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.get

fun Application.configureAuthentication() {
    val jwtConfig = get<JwtConfig>()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier())
            validate { credential ->
                val sub   = credential.payload.subject
                val email = credential.payload.getClaim("email").asString()
                if (!sub.isNullOrBlank() && !email.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            }
        }
    }
}
