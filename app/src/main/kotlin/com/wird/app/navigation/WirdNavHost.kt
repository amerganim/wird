package com.wird.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wird.feature.alarm.navigation.AlarmDestinations
import com.wird.feature.alarm.ui.AlarmRoute
import com.wird.feature.hifz.navigation.HifzDestinations
import com.wird.feature.hifz.ui.HifzReviewRoute
import com.wird.feature.hifz.ui.HifzRoute
import com.wird.feature.prayer.navigation.PrayerDestinations
import com.wird.feature.prayer.ui.PrayerRoute
import com.wird.feature.quran.navigation.QuranDestinations
import com.wird.feature.quran.ui.list.SurahListRoute
import com.wird.feature.quran.ui.reader.JuzReaderRoute
import com.wird.feature.quran.ui.reader.SurahReaderRoute

private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    QURAN(QuranDestinations.SURAH_LIST_ROUTE, "Quran", Icons.Outlined.MenuBook),
    PRAYER(PrayerDestinations.ROUTE, "Prayer", Icons.Outlined.Mosque),
    ALARM(AlarmDestinations.ROUTE, "Alarm", Icons.Outlined.Alarm),
    HIFZ(HifzDestinations.ROUTE, "Hifz", Icons.Outlined.Repeat),
}

@Composable
fun WirdApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = TopLevelDestination.entries.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = QuranDestinations.SURAH_LIST_ROUTE,
            modifier = Modifier.padding(innerPadding),
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
            composable(PrayerDestinations.ROUTE) {
                PrayerRoute()
            }
            composable(AlarmDestinations.ROUTE) {
                AlarmRoute()
            }
            composable(HifzDestinations.ROUTE) {
                HifzRoute(
                    onStartReview = { navController.navigate(HifzDestinations.REVIEW_ROUTE) },
                )
            }
            composable(HifzDestinations.REVIEW_ROUTE) {
                HifzReviewRoute(onFinish = { navController.popBackStack() })
            }
        }
    }
}
