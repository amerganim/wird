package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How often the user has stumbled on a given ayah (one row per ayah, created on the
 * first mistake). Feeds the Hifz mistake heatmap; currently logged when an ayah is
 * graded "Again" during review.
 */
@Entity(tableName = "mistake_stat")
data class MistakeStatEntity(
    @PrimaryKey val ayahId: Int,
    val mistakes: Int,
    val lastEpochDay: Long,
)
