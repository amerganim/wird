package com.wird.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wird.feature.quran.navigation.QuranDestinations
import com.wird.feature.quran.ui.list.SurahListRoute
import com.wird.feature.quran.ui.reader.JuzReaderRoute
import com.wird.feature.quran.ui.reader.SurahReaderRoute

@Composable
fun WirdApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = QuranDestinations.SURAH_LIST_ROUTE,
    ) {
        composable(QuranDestinations.SURAH_LIST_ROUTE) {
            SurahListRoute(
                onSurahClick = { surahNo ->
                    navController.navigate(QuranDestinations.readerRoute(surahNo))
                },
                onJuzClick = { juz ->
                    navController.navigate(QuranDestinations.juzReaderRoute(juz))
                },
                onBookmarkClick = { surahNo, ayahId ->
                    navController.navigate(QuranDestinations.readerRoute(surahNo, ayahId))
                },
            )
        }
        composable(
            route = QuranDestinations.READER_ROUTE,
            arguments = listOf(
                navArgument(QuranDestinations.SURAH_NO_ARG) { type = NavType.IntType },
                navArgument(QuranDestinations.AYAH_ID_ARG) {
                    type = NavType.IntType
                    defaultValue = QuranDestinations.NO_AYAH
                },
            ),
        ) {
            SurahReaderRoute(onBack = { navController.popBackStack() })
        }
        composable(
            route = QuranDestinations.JUZ_READER_ROUTE,
            arguments = listOf(
                navArgument(QuranDestinations.JUZ_ARG) { type = NavType.IntType },
            ),
        ) {
            JuzReaderRoute(onBack = { navController.popBackStack() })
        }
    }
}
