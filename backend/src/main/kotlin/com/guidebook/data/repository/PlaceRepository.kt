package com.guidebook.data.repository

import com.guidebook.db.tables.CategoriesTable
import com.guidebook.db.tables.PhotosTable
import com.guidebook.db.tables.PlacesTable
import com.guidebook.db.tables.ReviewsTable
import com.guidebook.db.tables.UsersTable
import com.guidebook.domain.model.Place
import com.guidebook.domain.model.PlaceStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

data class PagedResult<T>(val items: List<T>, val totalItems: Long)

interface PlaceRepository {
    suspend fun findApproved(categoryId: Int?, search: String?, page: Int, pageSize: Int, sortBy: String? = null): PagedResult<Place>
    suspend fun findById(id: UUID): Place?
    suspend fun findByUploader(userId: UUID): List<Place>
    suspend fun findPending(): List<Place>
    suspend fun create(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?,
        uploadedBy: UUID,
        status: PlaceStatus = PlaceStatus.PENDING
    ): Place
    suspend fun update(
        id: UUID,
        name: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?
    ): Place?
    suspend fun updateStatus(id: UUID, status: PlaceStatus): Place?
    suspend fun reject(id: UUID, reason: String?): Place?
    suspend fun updateCoverPath(id: UUID, path: String): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun getReviewStatsBatch(placeIds: List<UUID>): Map<UUID, Pair<Double, Int>>
    suspend fun getPhotosCountBatch(placeIds: List<UUID>): Map<UUID, Int>
}

class PlaceRepositoryImpl : PlaceRepository {

    override suspend fun findApproved(
        categoryId: Int?, search: String?, page: Int, pageSize: Int, sortBy: String?
    ): PagedResult<Place> = newSuspendedTransaction {
        var query = PlacesTable.select { PlacesTable.status eq PlaceStatus.APPROVED }
        categoryId?.let { cid ->
            query = query.andWhere { PlacesTable.categoryId eq EntityID(cid, CategoriesTable) }
        }
        search?.takeIf { it.isNotBlank() }?.let { s ->
            query = query.andWhere { PlacesTable.name.lowerCase() like "%${s.lowercase()}%" }
        }
        val total = query.count()
        val offset = ((page - 1) * pageSize).toLong()
        val items = when (sortBy) {
            "name" -> query.orderBy(PlacesTable.name, SortOrder.ASC)
            else   -> query.orderBy(PlacesTable.createdAt, SortOrder.DESC)
        }
            .limit(pageSize, offset)
            .map { it.toPlace() }
        PagedResult(items, total)
    }

    override suspend fun findById(id: UUID): Place? = newSuspendedTransaction {
        PlacesTable.select { PlacesTable.id eq id }
            .singleOrNull()?.toPlace()
    }

    override suspend fun findByUploader(userId: UUID): List<Place> = newSuspendedTransaction {
        PlacesTable.select { PlacesTable.uploadedBy eq EntityID(userId, UsersTable) }
            .orderBy(PlacesTable.createdAt, SortOrder.DESC)
            .map { it.toPlace() }
    }

    override suspend fun findPending(): List<Place> = newSuspendedTransaction {
        PlacesTable.select { PlacesTable.status eq PlaceStatus.PENDING }
            .orderBy(PlacesTable.createdAt, SortOrder.DESC)
            .map { it.toPlace() }
    }

    override suspend fun create(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?,
        uploadedBy: UUID,
        status: PlaceStatus
    ): Place = newSuspendedTransaction {
        val newId = PlacesTable.insertAndGetId {
            it[PlacesTable.name] = name
            it[PlacesTable.address] = address
            it[PlacesTable.latitude] = latitude
            it[PlacesTable.longitude] = longitude
            it[PlacesTable.categoryId] = categoryId?.let { cid -> EntityID(cid, CategoriesTable) }
            it[PlacesTable.description] = description
            it[PlacesTable.uploadedBy] = EntityID(uploadedBy, UsersTable)
            it[PlacesTable.status] = status
        }
        PlacesTable.select { PlacesTable.id eq newId }
            .single().toPlace()
    }

    override suspend fun update(
        id: UUID,
        name: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        categoryId: Int?,
        description: String?
    ): Place? = newSuspendedTransaction {
        val rowsUpdated = PlacesTable.update({ PlacesTable.id eq id }) {
            name?.let { n -> it[PlacesTable.name] = n }
            address?.let { a -> it[PlacesTable.address] = a }
            latitude?.let { lat -> it[PlacesTable.latitude] = lat }
            longitude?.let { lon -> it[PlacesTable.longitude] = lon }
            categoryId?.let { cid -> it[PlacesTable.categoryId] = EntityID(cid, CategoriesTable) }
            description?.let { d -> it[PlacesTable.description] = d }
            it[PlacesTable.updatedAt] = LocalDateTime.now()
        }
        if (rowsUpdated == 0) return@newSuspendedTransaction null
        PlacesTable.select { PlacesTable.id eq id }.single().toPlace()
    }

    override suspend fun updateStatus(id: UUID, status: PlaceStatus): Place? = newSuspendedTransaction {
        val rowsUpdated = PlacesTable.update({ PlacesTable.id eq id }) {
            it[PlacesTable.status] = status
            it[PlacesTable.updatedAt] = LocalDateTime.now()
        }
        if (rowsUpdated == 0) return@newSuspendedTransaction null
        PlacesTable.select { PlacesTable.id eq id }.single().toPlace()
    }

    override suspend fun reject(id: UUID, reason: String?): Place? = newSuspendedTransaction {
        val rowsUpdated = PlacesTable.update({ PlacesTable.id eq id }) {
            it[PlacesTable.status]          = PlaceStatus.REJECTED
            it[PlacesTable.rejectionReason] = reason
            it[PlacesTable.updatedAt]       = LocalDateTime.now()
        }
        if (rowsUpdated == 0) return@newSuspendedTransaction null
        PlacesTable.select { PlacesTable.id eq id }.single().toPlace()
    }

    override suspend fun updateCoverPath(id: UUID, path: String): Boolean = newSuspendedTransaction {
        PlacesTable.update({ PlacesTable.id eq id }) {
            it[PlacesTable.coverPath] = path
            it[PlacesTable.updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        PlacesTable.deleteWhere { PlacesTable.id eq id } > 0
    }

    override suspend fun getReviewStatsBatch(placeIds: List<UUID>): Map<UUID, Pair<Double, Int>> {
        if (placeIds.isEmpty()) return emptyMap()
        val entityIds = placeIds.map { EntityID(it, PlacesTable) }
        val avgExpr = ReviewsTable.rating.avg()
        val countExpr = ReviewsTable.id.count()
        return newSuspendedTransaction {
            ReviewsTable
                .slice(ReviewsTable.placeId, avgExpr, countExpr)
                .select { ReviewsTable.placeId inList entityIds }
                .groupBy(ReviewsTable.placeId)
                .associate { row ->
                    row[ReviewsTable.placeId].value to Pair(
                        row[avgExpr]?.toDouble() ?: 0.0,
                        row[countExpr].toInt()
                    )
                }
        }
    }

    override suspend fun getPhotosCountBatch(placeIds: List<UUID>): Map<UUID, Int> {
        if (placeIds.isEmpty()) return emptyMap()
        val entityIds = placeIds.map { EntityID(it, PlacesTable) }
        val countExpr = PhotosTable.id.count()
        return newSuspendedTransaction {
            PhotosTable
                .slice(PhotosTable.placeId, countExpr)
                .select { PhotosTable.placeId inList entityIds }
                .groupBy(PhotosTable.placeId)
                .associate { row ->
                    row[PhotosTable.placeId].value to row[countExpr].toInt()
                }
        }
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
        rejectionReason = this[PlacesTable.rejectionReason],
        createdAt = this[PlacesTable.createdAt],
        updatedAt = this[PlacesTable.updatedAt]
    )
}
