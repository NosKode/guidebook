package com.guidebook.service

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun `hash returns bcrypt hash`() {
        val hash = PasswordHasher.hash("password123")
        assertTrue(hash.startsWith("\$2a\$") || hash.startsWith("\$2b\$"))
    }

    @Test
    fun `hash produces different hashes for same password due to random salt`() {
        val hash1 = PasswordHasher.hash("password123")
        val hash2 = PasswordHasher.hash("password123")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `verify returns true for correct password`() {
        val hash = PasswordHasher.hash("correctPassword")
        assertTrue(PasswordHasher.verify("correctPassword", hash))
    }

    @Test
    fun `verify returns false for wrong password`() {
        val hash = PasswordHasher.hash("correctPassword")
        assertFalse(PasswordHasher.verify("wrongPassword", hash))
    }

    @Test
    fun `verify returns false for empty string against real hash`() {
        val hash = PasswordHasher.hash("password123")
        assertFalse(PasswordHasher.verify("", hash))
    }

    @Test
    fun `verify is case-sensitive`() {
        val hash = PasswordHasher.hash("Password123")
        assertFalse(PasswordHasher.verify("password123", hash))
    }

    @Test
    fun `verify returns false for substring of original password`() {
        val hash = PasswordHasher.hash("password123")
        assertFalse(PasswordHasher.verify("password", hash))
    }
}
