package com.guidebook.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ReviewsTable : UUIDTable("reviews") {
    val placeId   = reference("place_id", PlacesTable, onDelete = ReferenceOption.CASCADE)
    val userId    = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val rating    = integer("rating").check { (it greaterEq 1) and (it lessEq 5) }
    val comment   = text("comment").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(placeId, userId)
    }
}
