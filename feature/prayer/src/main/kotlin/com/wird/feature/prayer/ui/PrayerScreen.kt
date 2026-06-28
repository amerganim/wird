package com.wird.feature.prayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PrayerRoute(viewModel: PrayerViewModel = hiltViewModel()) {
    PrayerScreen(viewModel.uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(uiState: PrayerUiState) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Prayer Times") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "${uiState.locationName} · ${uiState.dateLabel}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Method: ${uiState.method}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            uiState.times.forEach { row ->
                ListItem(
                    headlineContent = { Text(row.name) },
                    trailingContent = {
                        Text(row.time, style = MaterialTheme.typography.titleMedium)
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
