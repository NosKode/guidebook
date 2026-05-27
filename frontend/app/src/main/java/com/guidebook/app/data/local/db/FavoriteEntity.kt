package com.guidebook.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverUrl: String?,
    val address: String?,
    val averageRating: Double,
    val cachedAt: Long
)
