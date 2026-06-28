package com.wird.feature.quran.ui.reader

import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.SurahEntity

/** A single rendered row in a reader (surah or juz). */
sealed interface ReaderItem {
    val key: String

    data class SurahHeader(val surah: SurahEntity) : ReaderItem {
        override val key: String = "header-${surah.number}"
    }

    data class Bismillah(val surahNumber: Int) : ReaderItem {
        override val key: String = "bismillah-$surahNumber"
    }

    data class AyahLine(val ayah: AyahEntity) : ReaderItem {
        override val key: String = "ayah-${ayah.id}"
    }
}

/** Surahs whose bismillah is not rendered separately (Al-Fatiha includes it as
 * ayah 1; At-Tawba has none). */
private val SURAHS_WITHOUT_SEPARATE_BISMILLAH = setOf(1, 9)

internal fun shouldShowBismillah(surahNumber: Int, ayahNo: Int): Boolean =
    ayahNo == 1 && surahNumber !in SURAHS_WITHOUT_SEPARATE_BISMILLAH
