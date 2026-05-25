package com.guidebook.data.repository

import com.guidebook.db.tables.PlacesTable
import com.guidebook.db.tables.ReviewsTable
import com.guidebook.db.tables.UsersTable
import com.guidebook.domain.model.Review
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class ReviewRow(val review: Review, val userName: String?)

interface ReviewRepository {
    suspend fun findByPlace(placeId: UUID): List<ReviewRow>
    suspend fun findById(id: UUID): Review?
    suspend fun findByUserAndPlace(userId: UUID, placeId: UUID): Review?
    suspend fun create(placeId: UUID, userId: UUID, rating: Int, comment: String?): Review
    suspend fun update(id: UUID, rating: Int?, comment: String?): Review?
    suspend fun delete(id: UUID): Boolean
    suspend fun averageRating(placeId: UUID): Double
    suspend fun countByPlace(placeId: UUID): Int
}

class ReviewRepositoryImpl : ReviewRepository {

    override suspend fun findByPlace(placeId: UUID): List<ReviewRow> = newSuspendedTransaction {
        ReviewsTable
            .join(UsersTable, JoinType.LEFT, ReviewsTable.userId, UsersTable.id)
            .select { ReviewsTable.placeId eq EntityID(placeId, PlacesTable) }
            .orderBy(ReviewsTable.createdAt, SortOrder.DESC)
            .map { ReviewRow(review = it.toReview(), userName = it[UsersTable.displayName]) }
    }

    override suspend fun findById(id: UUID): Review? = newSuspendedTransaction {
        ReviewsTable.select { ReviewsTable.id eq id }.singleOrNull()?.toReview()
    }

    override suspend fun findByUserAndPlace(userId: UUID, placeId: UUID): Review? =
        newSuspendedTransaction {
            ReviewsTable.select {
                (ReviewsTable.userId eq EntityID(userId, UsersTable)) and
                (ReviewsTable.placeId eq EntityID(placeId, PlacesTable))
            }.singleOrNull()?.toReview()
        }

    override suspend fun create(placeId: UUID, userId: UUID, rating: Int, comment: String?): Review =
        newSuspendedTransaction {
            val newId = ReviewsTable.insertAndGetId {
                it[ReviewsTable.placeId] = EntityID(placeId, PlacesTable)
                it[ReviewsTable.userId]  = EntityID(userId, UsersTable)
                it[ReviewsTable.rating]  = rating
                it[ReviewsTable.comment] = comment
            }
            ReviewsTable.select { ReviewsTable.id eq newId }.single().toReview()
        }

    override suspend fun update(id: UUID, rating: Int?, comment: String?): Review? =
        newSuspendedTransaction {
            val rowsUpdated = ReviewsTable.update({ ReviewsTable.id eq id }) {
                rating?.let { r -> it[ReviewsTable.rating] = r }
                comment?.let { c -> it[ReviewsTable.comment] = c }
            }
            if (rowsUpdated == 0) return@newSuspendedTransaction null
            ReviewsTable.select { ReviewsTable.id eq id }.single().toReview()
        }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        ReviewsTable.deleteWhere { ReviewsTable.id eq id } > 0
    }

    override suspend fun averageRating(placeId: UUID): Double = newSuspendedTransaction {
        val avgExpr = ReviewsTable.rating.avg()
        ReviewsTable
            .slice(avgExpr)
            .select { ReviewsTable.placeId eq EntityID(placeId, PlacesTable) }
            .firstOrNull()?.get(avgExpr)?.toDouble() ?: 0.0
    }

    override suspend fun countByPlace(placeId: UUID): Int = newSuspendedTransaction {
        ReviewsTable.select { ReviewsTable.placeId eq EntityID(placeId, PlacesTable) }
            .count().toInt()
    }

    private fun ResultRow.toReview() = Review(
        id = this[ReviewsTable.id].value,
        placeId = this[ReviewsTable.placeId].value,
        userId = this[ReviewsTable.userId].value,
        rating = this[ReviewsTable.rating],
        comment = this[ReviewsTable.comment],
        createdAt = this[ReviewsTable.createdAt]
    )
}
