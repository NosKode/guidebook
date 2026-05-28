package com.guidebook.data.repository

import com.guidebook.db.tables.UsersTable
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun create(email: String, passwordHash: String, displayName: String?): User
    suspend fun updateAvatar(id: UUID, avatarPath: String): User
}

class UserRepositoryImpl : UserRepository {

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UsersTable.select { UsersTable.email eq email }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UsersTable.select { UsersTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun create(
        email: String,
        passwordHash: String,
        displayName: String?
    ): User = newSuspendedTransaction {
        val id = UsersTable.insertAndGetId {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.displayName] = displayName
            it[UsersTable.role] = UserRole.USER
        }
        UsersTable.select { UsersTable.id eq id }
            .single()
            .toUser()
    }

    override suspend fun updateAvatar(id: UUID, avatarPath: String): User = newSuspendedTransaction {
        UsersTable.update({ UsersTable.id eq id }) {
            it[UsersTable.avatarPath] = avatarPath
        }
        UsersTable.select { UsersTable.id eq id }.single().toUser()
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id].value,
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        displayName = this[UsersTable.displayName],
        role = this[UsersTable.role],
        createdAt = this[UsersTable.createdAt],
        avatarPath = this[UsersTable.avatarPath]
    )
}
