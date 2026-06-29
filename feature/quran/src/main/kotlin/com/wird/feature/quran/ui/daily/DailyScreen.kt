package com.wird.feature.quran.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.quran.data.DailyAyahItem

@Composable
fun DailyRoute(
    onBack: () -> Unit,
    viewModel: DailyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DailyScreen(uiState = uiState, onBack = onBack, onMarkRead = viewModel::markRead)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    uiState: DailyUiState,
    onBack: () -> Unit,
    onMarkRead: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily ayat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StreakHeader(uiState.currentStreak, uiState.bestStreak) }

            items(uiState.ayat, key = { it.ayah.id }) { item ->
                DailyAyahCard(item)
            }

            item {
                Button(
                    onClick = onMarkRead,
                    enabled = !uiState.readToday,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Text(if (uiState.readToday) "Read today ✓" else "Mark as read")
                }
            }
        }
    }
}

@Composable
private fun StreakHeader(currentStreak: Int, bestStreak: Int) {
    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🔥 $currentStreak", fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
            Text(
                if (currentStreak == 1) "day streak" else "days streak",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Best: $bestStreak " + if (bestStreak == 1) "day" else "days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailyAyahCard(item: DailyAyahItem) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${item.surahNameTranslit} · ${item.ayah.ayahNo}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.ayah.textUthmani,
                style = ArabicAyahTextStyle,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
