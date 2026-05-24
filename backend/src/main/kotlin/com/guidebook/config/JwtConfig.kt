package com.guidebook.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import java.util.*

class JwtConfig(application: Application) {
    private val config = application.environment.config

    val secret: String = config.property("jwt.secret").getString()
    val issuer: String = config.property("jwt.issuer").getString()
    val audience: String = config.property("jwt.audience").getString()
    val realm: String = config.property("jwt.realm").getString()

    private val algorithm = Algorithm.HMAC256(secret)
    private val tokenTtlMs = 24 * 60 * 60 * 1000L

    fun generateToken(userId: Int, email: String, role: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenTtlMs))
            .sign(algorithm)
}
