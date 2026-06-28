package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-saved ayah. Keyed by the global ayah id. */
@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey val ayahId: Int,
    val createdAt: Long,
)
