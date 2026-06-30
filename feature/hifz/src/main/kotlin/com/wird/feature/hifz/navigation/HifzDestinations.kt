package com.wird.feature.hifz.navigation

object HifzDestinations {
    const val ROUTE = "hifz"
    const val REVIEW_ROUTE = "hifz/review"

    const val SURAH_NO_ARG = "surahNo"
    const val PRACTICE_ROUTE = "hifz/practice/{$SURAH_NO_ARG}"
    fun practiceRoute(surahNo: Int): String = "hifz/practice/$surahNo"
}
