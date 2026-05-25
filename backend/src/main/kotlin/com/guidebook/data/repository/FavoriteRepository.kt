package com.guidebook.data.repository

import com.guidebook.db.tables.FavoritesTable
import com.guidebook.db.tables.PlacesTable
import com.guidebook.db.tables.UsersTable
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface FavoriteRepository {
    suspend fun findByUser(userId: UUID): List<Place>
    suspend fun exists(userId: UUID, placeId: UUID): Boolean
    suspend fun add(userId: UUID, placeId: UUID): Boolean
    suspend fun remove(userId: UUID, placeId: UUID): Boolean
}

class FavoriteRepositoryImpl : FavoriteRepository {

    override suspend fun findByUser(userId: UUID): List<Place> = newSuspendedTransaction {
        FavoritesTable
            .join(PlacesTable, JoinType.INNER, FavoritesTable.placeId, PlacesTable.id)
            .select {
                (FavoritesTable.userId eq EntityID(userId, UsersTable)) and
                (PlacesTable.status eq PlaceStatus.APPROVED)
            }
            .orderBy(FavoritesTable.addedAt, SortOrder.DESC)
            .map { it.toPlace() }
    }

    override suspend fun exists(userId: UUID, placeId: UUID): Boolean = newSuspendedTransaction {
        FavoritesTable.select {
            (FavoritesTable.userId  eq EntityID(userId, UsersTable)) and
            (FavoritesTable.placeId eq EntityID(placeId, PlacesTable))
        }.count() > 0
    }

    override suspend fun add(userId: UUID, placeId: UUID): Boolean = newSuspendedTransaction {
        val alreadyExists = FavoritesTable.select {
            (FavoritesTable.userId  eq EntityID(userId, UsersTable)) and
            (FavoritesTable.placeId eq EntityID(placeId, PlacesTable))
        }.count() > 0
        if (alreadyExists) return@newSuspendedTransaction false
        FavoritesTable.insert {
            it[FavoritesTable.userId]  = EntityID(userId, UsersTable)
            it[FavoritesTable.placeId] = EntityID(placeId, PlacesTable)
        }
        true
    }

    override suspend fun remove(userId: UUID, placeId: UUID): Boolean = newSuspendedTransaction {
        FavoritesTable.deleteWhere {
            (FavoritesTable.userId  eq EntityID(userId, UsersTable)) and
            (FavoritesTable.placeId eq EntityID(placeId, PlacesTable))
        } > 0
    }

    private fun ResultRow.toPlace() = Place(
        id = this[PlacesTable.id].value,
        name = this[PlacesTable.name],
        address = this[PlacesTable.address],
        latitude = this[PlacesTable.latitude],
        longitude = this[PlacesTable.longitude],
        categoryId = this[PlacesTable.categoryId]?.value,
        description = this[PlacesTable.description],
        coverPath = this[PlacesTable.coverPath],
        uploadedBy = this[PlacesTable.uploadedBy]?.value,
        status = this[PlacesTable.status],
        createdAt = this[PlacesTable.createdAt],
        updatedAt = this[PlacesTable.updatedAt]
    )
}
