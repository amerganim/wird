package com.wird.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wird.feature.hifz.HifzScreen
import com.wird.feature.prayer.PrayerScreen
import com.wird.feature.qibla.QiblaScreen
import com.wird.feature.quran.QuranScreen

/** Top-level destinations surfaced in the bottom navigation bar. */
private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    QURAN("quran", "Quran", Icons.Outlined.MenuBook),
    PRAYER("prayer", "Prayer", Icons.Outlined.Mosque),
    QIBLA("qibla", "Qibla", Icons.Outlined.Explore),
    HIFZ("hifz", "Hifz", Icons.Outlined.Repeat),
}

@Composable
fun WirdApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.QURAN.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.QURAN.route) { QuranScreen() }
            composable(TopLevelDestination.PRAYER.route) { PrayerScreen() }
            composable(TopLevelDestination.QIBLA.route) { QiblaScreen() }
            composable(TopLevelDestination.HIFZ.route) { HifzScreen() }
        }
    }
}
