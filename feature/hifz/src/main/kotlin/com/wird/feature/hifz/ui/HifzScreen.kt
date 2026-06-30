package com.wird.feature.hifz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HifzRoute(
    onStartReview: () -> Unit,
    onPracticeSurah: (Int) -> Unit,
    viewModel: HifzViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HifzScreen(
        state = uiState,
        onStartReview = onStartReview,
        onAddSurah = viewModel::addSurah,
        onPracticeSurah = onPracticeSurah,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HifzScreen(
    state: HifzDashboardState,
    onStartReview: () -> Unit,
    onAddSurah: (Int) -> Unit,
    onPracticeSurah: (Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Hifz") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 24.dp),
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Due for review", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("${state.dueCount}", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${state.totalCount} ayat memorizing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onStartReview,
                    enabled = state.dueCount > 0,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.dueCount > 0) "Review ${state.dueCount} now" else "Nothing due") }
            }

            item {
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Add a surah to memorize") }
            }

            if (state.totalCount == 0) {
                item {
                    Text(
                        "Add a surah, then review daily. Spaced repetition (SM-2) schedules " +
                            "each ayah so you revise right before you'd forget it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.memorizing.isNotEmpty()) {
                item {
                    Text(
                        "Practice (progressive blanking)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(state.memorizing, key = { it.surahNo }) { surah ->
                    ListItem(
                        modifier = Modifier.clickable { onPracticeSurah(surah.surahNo) },
                        headlineContent = { Text(surah.nameTranslit) },
                        supportingContent = { Text("${surah.count} ayat") },
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showPicker) {
            SurahPickerSheet(
                surahs = state.surahs,
                onPick = {
                    onAddSurah(it)
                    showPicker = false
                },
                onDismiss = { showPicker = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahPickerSheet(
    surahs: List<com.wird.core.database.entity.SurahEntity>,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Add a surah",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
            )
            LazyColumn(Modifier.fillMaxWidth()) {
                items(surahs, key = { it.number }) { surah ->
                    ListItem(
                        modifier = Modifier.clickable { onPick(surah.number) },
                        headlineContent = { Text("${surah.number}. ${surah.nameTranslit}") },
                        supportingContent = { Text("${surah.ayahCount} ayahs") },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
