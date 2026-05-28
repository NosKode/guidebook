package com.guidebook.service

import com.guidebook.config.JwtConfig
import com.guidebook.data.dto.LoginRequest
import com.guidebook.data.dto.RegisterRequest
import com.guidebook.data.repository.UserRepository
import com.guidebook.domain.exception.BadRequestException
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.exception.UnauthorizedException
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val jwtConfig: JwtConfig = mockk()
    private val fileStorageService: FileStorageService = mockk()

    private val service = AuthService(userRepository, jwtConfig, fileStorageService, "http://localhost:8080")

    private val userId = UUID.randomUUID()
    private val fakeUser = User(
        id = userId,
        email = "test@example.com",
        passwordHash = PasswordHasher.hash("password123"),
        displayName = "Test User",
        role = UserRole.USER,
        createdAt = LocalDateTime.now(),
        avatarPath = null
    )

    @BeforeEach
    fun setUp() {
        every { jwtConfig.makeToken(any(), any(), any()) } returns "fake-jwt-token"
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    fun `register with invalid email throws BadRequestException`() = runTest {
        assertFailsWith<BadRequestException> {
            service.register(RegisterRequest("not-an-email", "password123"))
        }
    }

    @Test
    fun `register with email missing domain throws BadRequestException`() = runTest {
        assertFailsWith<BadRequestException> {
            service.register(RegisterRequest("user@", "password123"))
        }
    }

    @Test
    fun `register with password shorter than 8 chars throws BadRequestException`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        assertFailsWith<BadRequestException> {
            service.register(RegisterRequest("test@example.com", "short"))
        }
    }

    @Test
    fun `register with exactly 7 char password throws BadRequestException`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        assertFailsWith<BadRequestException> {
            service.register(RegisterRequest("test@example.com", "1234567"))
        }
    }

    @Test
    fun `register with already taken email throws ConflictException`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns fakeUser
        assertFailsWith<ConflictException> {
            service.register(RegisterRequest("test@example.com", "password123"))
        }
    }

    @Test
    fun `register success returns AuthResponse with token`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        coEvery { userRepository.create(any(), any(), any()) } returns fakeUser
        val response = service.register(RegisterRequest("test@example.com", "password123"))
        assertEquals("fake-jwt-token", response.token)
        assertEquals("test@example.com", response.user.email)
    }

    @Test
    fun `register trims and lowercases email`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns null
        coEvery { userRepository.create("test@example.com", any(), any()) } returns fakeUser
        service.register(RegisterRequest("  TEST@EXAMPLE.COM  ", "password123"))
        coVerify { userRepository.create("test@example.com", any(), any()) }
    }

    @Test
    fun `register with blank displayName passes null to repository`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        coEvery { userRepository.create(any(), any(), null) } returns fakeUser
        service.register(RegisterRequest("test@example.com", "password123", "   "))
        coVerify { userRepository.create(any(), any(), null) }
    }

    @Test
    fun `register with non-blank displayName passes trimmed value`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        coEvery { userRepository.create(any(), any(), "Alice") } returns fakeUser
        service.register(RegisterRequest("test@example.com", "password123", "  Alice  "))
        coVerify { userRepository.create(any(), any(), "Alice") }
    }

    @Test
    fun `register success returns user with USER role`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        coEvery { userRepository.create(any(), any(), any()) } returns fakeUser
        val response = service.register(RegisterRequest("test@example.com", "password123"))
        assertEquals("USER", response.user.role)
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login with unknown email throws UnauthorizedException`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null
        assertFailsWith<UnauthorizedException> {
            service.login(LoginRequest("unknown@example.com", "password123"))
        }
    }

    @Test
    fun `login with wrong password throws UnauthorizedException`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns fakeUser
        assertFailsWith<UnauthorizedException> {
            service.login(LoginRequest("test@example.com", "wrongPassword"))
        }
    }

    @Test
    fun `login with correct credentials returns AuthResponse`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns fakeUser
        val response = service.login(LoginRequest("test@example.com", "password123"))
        assertEquals("fake-jwt-token", response.token)
        assertEquals("test@example.com", response.user.email)
    }

    @Test
    fun `login trims and lowercases email`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns fakeUser
        val response = service.login(LoginRequest("  TEST@EXAMPLE.COM  ", "password123"))
        assertEquals("fake-jwt-token", response.token)
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    fun `getUserById with unknown id throws NotFoundException`() = runTest {
        coEvery { userRepository.findById(any()) } returns null
        assertFailsWith<NotFoundException> { service.getUserById(UUID.randomUUID()) }
    }

    @Test
    fun `getUserById with known id returns user`() = runTest {
        coEvery { userRepository.findById(userId) } returns fakeUser
        val user = service.getUserById(userId)
        assertEquals(userId, user.id)
        assertEquals("test@example.com", user.email)
    }
}
