package com.guidebook.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object PhotosTable : UUIDTable("photos") {
    val placeId   = reference("place_id", PlacesTable, onDelete = ReferenceOption.CASCADE)
    val filePath  = varchar("file_path", 500)
    val caption   = varchar("caption", 255).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
