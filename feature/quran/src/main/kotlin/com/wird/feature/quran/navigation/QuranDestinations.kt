package com.wird.feature.quran.navigation

/** Routes owned by the Quran feature. */
object QuranDestinations {
    const val SURAH_LIST_ROUTE = "quran/surahs"

    const val SURAH_NO_ARG = "surahNo"
    const val READER_ROUTE = "quran/reader/{$SURAH_NO_ARG}"

    fun readerRoute(surahNo: Int): String = "quran/reader/$surahNo"
}
