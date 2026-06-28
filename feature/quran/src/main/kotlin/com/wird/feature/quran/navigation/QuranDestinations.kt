package com.wird.feature.quran.navigation

/** Routes owned by the Quran feature. */
object QuranDestinations {
    const val SURAH_LIST_ROUTE = "quran/surahs"

    const val SURAH_NO_ARG = "surahNo"
    const val AYAH_ID_ARG = "ayahId"

    /** Optional `ayahId` query arg scrolls the reader straight to that ayah. */
    const val READER_ROUTE = "quran/reader/{$SURAH_NO_ARG}?$AYAH_ID_ARG={$AYAH_ID_ARG}"
    const val NO_AYAH = -1

    const val JUZ_ARG = "juz"
    const val JUZ_READER_ROUTE = "quran/juz/{$JUZ_ARG}"

    fun readerRoute(surahNo: Int, ayahId: Int? = null): String =
        "quran/reader/$surahNo" + (ayahId?.let { "?$AYAH_ID_ARG=$it" } ?: "")

    fun juzReaderRoute(juz: Int): String = "quran/juz/$juz"
}
