package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Read-only Quran content: one row per ayah (shipped, seeded from assets).
 * [id] is the global ayah number (1..6236); [textUthmani] is the unmodified
 * Tanzil Uthmani text.
 */
@Entity(
    tableName = "ayah",
    indices = [Index(value = ["surahNo", "ayahNo"])],
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    val surahNo: Int,
    val ayahNo: Int,
    val juz: Int,
    val hizb: Int,
    val page: Int,
    val sajda: Boolean,
    val textUthmani: String,
)
