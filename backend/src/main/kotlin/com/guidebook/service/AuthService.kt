package com.guidebook.service

import com.guidebook.config.JwtConfig
import com.guidebook.data.dto.AuthResponse
import com.guidebook.data.dto.LoginRequest
import com.guidebook.data.dto.RegisterRequest
import com.guidebook.data.dto.UserDto
import com.guidebook.data.repository.UserRepository
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.exception.UnauthorizedException
import com.guidebook.domain.model.User
import io.ktor.http.content.*
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val jwtConfig: JwtConfig,
    private val fileStorageService: FileStorageService,
    private val baseUrl: String
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        if (!email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")))
            throw BadRequestException("Invalid email format")
        if (request.password.length < 8)
            throw BadRequestException("Password must be at least 8 characters")

        if (userRepository.findByEmail(email) != null)
            throw ConflictException("Email is already taken")

        val user = userRepository.create(
            email = email,
            passwordHash = PasswordHasher.hash(request.password),
            displayName = request.displayName?.trim()?.takeIf { it.isNotBlank() }
        )
        return buildResponse(user)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!PasswordHasher.verify(request.password, user.passwordHash))
            throw UnauthorizedException("Invalid credentials")

        return buildResponse(user)
    }

    suspend fun getUserById(id: UUID): User =
        userRepository.findById(id) ?: throw NotFoundException("User not found")

    suspend fun uploadAvatar(userId: UUID, part: PartData.FileItem): UserDto {
        val relativePath = fileStorageService.saveAvatar(userId, part)
        val user = userRepository.updateAvatar(userId, relativePath)
        return user.toDto(baseUrl)
    }

    fun userToDto(user: User): UserDto = user.toDto(baseUrl)

    private fun buildResponse(user: User): AuthResponse {
        val token = jwtConfig.makeToken(user.id, user.email, user.role.name)
        return AuthResponse(token = token, user = user.toDto(baseUrl))
    }
}

fun User.toDto(baseUrl: String) = UserDto(
    id = id.toString(),
    email = email,
    displayName = displayName,
    role = role.name,
    createdAt = createdAt.toString(),
    avatarUrl = avatarPath?.let { "$baseUrl/files/images/$it" }
)
