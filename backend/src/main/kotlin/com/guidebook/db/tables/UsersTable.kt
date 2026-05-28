package com.guidebook.db.tables

import com.guidebook.domain.model.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : UUIDTable("users") {
    val email        = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName  = varchar("display_name", 100).nullable()
    val role         = pgEnum<UserRole>("role", "user_role")
    val createdAt    = datetime("created_at").defaultExpression(CurrentDateTime)
    val avatarPath   = varchar("avatar_path", 500).nullable()
}
