package com.guidebook.data.repository

import com.guidebook.db.tables.PhotosTable
import com.guidebook.db.tables.PlacesTable
import com.guidebook.domain.model.Photo
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface PhotoRepository {
    suspend fun findByPlace(placeId: UUID): List<Photo>
    suspend fun findById(id: UUID): Photo?
    suspend fun create(placeId: UUID, filePath: String, caption: String?): Photo
    suspend fun delete(id: UUID): Boolean
    suspend fun countByPlace(placeId: UUID): Int
}

class PhotoRepositoryImpl : PhotoRepository {

    override suspend fun findByPlace(placeId: UUID): List<Photo> = newSuspendedTransaction {
        PhotosTable.select { PhotosTable.placeId eq EntityID(placeId, PlacesTable) }
            .orderBy(PhotosTable.createdAt, SortOrder.DESC)
            .map { it.toPhoto() }
    }

    override suspend fun findById(id: UUID): Photo? = newSuspendedTransaction {
        PhotosTable.select { PhotosTable.id eq id }
            .singleOrNull()?.toPhoto()
    }

    override suspend fun create(placeId: UUID, filePath: String, caption: String?): Photo =
        newSuspendedTransaction {
            val newId = PhotosTable.insertAndGetId {
                it[PhotosTable.placeId] = EntityID(placeId, PlacesTable)
                it[PhotosTable.filePath] = filePath
                it[PhotosTable.caption] = caption
            }
            PhotosTable.select { PhotosTable.id eq newId }
                .single().toPhoto()
        }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        PhotosTable.deleteWhere { PhotosTable.id eq id } > 0
    }

    override suspend fun countByPlace(placeId: UUID): Int = newSuspendedTransaction {
        PhotosTable.select { PhotosTable.placeId eq EntityID(placeId, PlacesTable) }
            .count().toInt()
    }

    private fun ResultRow.toPhoto() = Photo(
        id = this[PhotosTable.id].value,
        placeId = this[PhotosTable.placeId].value,
        filePath = this[PhotosTable.filePath],
        caption = this[PhotosTable.caption],
        createdAt = this[PhotosTable.createdAt]
    )
}
