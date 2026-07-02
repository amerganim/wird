package com.wird.feature.recitation.navigation

object RecitationDestinations {
    const val SURAH_NO_ARG = "surahNo"
    const val ROUTE = "recitation/{$SURAH_NO_ARG}"
    fun route(surahNo: Int): String = "recitation/$surahNo"

    const val FOLLOW_ROUTE = "recitation/follow/{$SURAH_NO_ARG}"
    fun followRoute(surahNo: Int): String = "recitation/follow/$surahNo"
}
