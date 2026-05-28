package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.UserDto
import com.guidebook.app.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMappersTest {

    private fun makeDto(role: String = "USER") = UserDto(
        id = "user-1",
        email = "test@example.com",
        displayName = "Test User",
        role = role,
        createdAt = "2024-01-01T00:00:00Z",
        avatarUrl = null
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val domain = makeDto().copy(avatarUrl = "http://example.com/avatar.jpg").toDomain()
        assertEquals("user-1", domain.id)
        assertEquals("test@example.com", domain.email)
        assertEquals("Test User", domain.displayName)
        assertEquals("http://example.com/avatar.jpg", domain.avatarUrl)
        assertEquals("2024-01-01T00:00:00Z", domain.createdAt)
    }

    @Test
    fun `toDomain maps USER role`() {
        assertEquals(UserRole.USER, makeDto("USER").toDomain().role)
    }

    @Test
    fun `toDomain maps ADMIN role`() {
        assertEquals(UserRole.ADMIN, makeDto("ADMIN").toDomain().role)
    }

    @Test
    fun `toDomain falls back to USER for unknown role`() {
        assertEquals(UserRole.USER, makeDto("SUPERADMIN").toDomain().role)
    }

    @Test
    fun `toDomain falls back to USER for empty role string`() {
        assertEquals(UserRole.USER, makeDto("").toDomain().role)
    }

    @Test
    fun `toDomain handles null displayName and avatarUrl`() {
        val domain = makeDto().copy(displayName = null, avatarUrl = null).toDomain()
        assertNull(domain.displayName)
        assertNull(domain.avatarUrl)
    }
}
