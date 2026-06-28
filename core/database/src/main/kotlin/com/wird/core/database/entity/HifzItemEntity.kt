package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** An ayah under memorization, with SM-2 spaced-repetition scheduling fields. */
@Entity(
    tableName = "hifz_item",
    indices = [Index(value = ["dueEpochDay"])],
)
data class HifzItemEntity(
    @PrimaryKey val ayahId: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int,
    val dueEpochDay: Long,
    val lastReviewedEpochDay: Long,
    val lapses: Int,
)
