package com.guidebook.db.tables

import com.guidebook.domain.model.PlaceStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object PlacesTable : UUIDTable("places") {
    val name        = varchar("name", 255)
    val address     = varchar("address", 500).nullable()
    val latitude    = double("latitude").nullable()
    val longitude   = double("longitude").nullable()
    val categoryId  = optReference("category_id", CategoriesTable)
    val description = text("description").nullable()
    val coverPath   = varchar("cover_path", 500).nullable()
    val uploadedBy  = optReference("uploaded_by", UsersTable, onDelete = ReferenceOption.SET_NULL)
    val status      = pgEnum<PlaceStatus>("status", "place_status")
    val createdAt   = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt   = datetime("updated_at").defaultExpression(CurrentDateTime)
}
