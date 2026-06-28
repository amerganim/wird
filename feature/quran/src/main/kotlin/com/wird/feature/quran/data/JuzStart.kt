package com.wird.feature.quran.data

/** Where a juz begins — used for the juz list. */
data class JuzStart(
    val juz: Int,
    val surahNo: Int,
    val surahNameTranslit: String,
    val ayahNo: Int,
)
