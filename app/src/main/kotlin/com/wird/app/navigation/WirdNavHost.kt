package com.wird.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wird.feature.quran.QuranScreen

private const val QURAN_ROUTE = "quran"

@Composable
fun WirdApp() {
    val navController = rememberNavController()

    // Single feature for now (the Quran reader). A bottom-nav bar and the other
    // top-level destinations return as their feature modules are reintroduced.
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = QURAN_ROUTE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(QURAN_ROUTE) { QuranScreen() }
        }
    }
}
