package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Read-only Quran content: one row per surah (shipped, seeded from assets). */
@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameAr: String,
    val nameTranslit: String,
    val nameEn: String,
    val revelationPlace: String,
    val ayahCount: Int,
)
