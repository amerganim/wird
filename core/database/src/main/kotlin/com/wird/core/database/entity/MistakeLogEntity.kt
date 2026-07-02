package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A granular record of one recitation mistake: which ayah, which word position, and
 * what kind (MISSED / SUBSTITUTED / EXTRA). The aggregated [MistakeStatEntity] powers
 * the heatmap; this log keeps the per-word detail for future review/trends.
 */
@Entity(
    tableName = "mistake_log",
    indices = [Index(value = ["ayahId"])],
)
data class MistakeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ayahId: Int,
    val wordPosition: Int,
    val mistakeType: String,
    val createdAt: Long,
)
