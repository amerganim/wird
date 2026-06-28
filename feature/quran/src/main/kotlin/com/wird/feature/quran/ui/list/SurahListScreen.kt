package com.wird.feature.quran.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.database.entity.SurahEntity
import com.wird.core.ui.theme.ArabicAyahTextStyle

@Composable
fun SurahListRoute(
    onSurahClick: (Int) -> Unit,
    viewModel: SurahListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SurahListScreen(uiState = uiState, onSurahClick = onSurahClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    uiState: SurahListUiState,
    onSurahClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Wird") }) },
    ) { innerPadding ->
        when (uiState) {
            SurahListUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is SurahListUiState.Content -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                uiState.continueReading?.let { cont ->
                    item {
                        ContinueReadingCard(
                            continueReading = cont,
                            onClick = { onSurahClick(cont.surahNumber) },
                        )
                    }
                }
                items(uiState.surahs, key = { it.number }) { surah ->
                    SurahRow(surah = surah, onClick = { onSurahClick(surah.number) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    continueReading: ContinueReading,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .padding(16.dp)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Continue reading",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${continueReading.surahNameTranslit} · ayah ${continueReading.ayahNo}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SurahRow(
    surah: SurahEntity,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { SurahNumberBadge(surah.number) },
        headlineContent = { Text(surah.nameTranslit) },
        supportingContent = {
            Text("${surah.nameEn} · ${surah.ayahCount} ayahs · ${surah.revelationPlace}")
        },
        trailingContent = {
            Text(
                text = surah.nameAr,
                style = ArabicAyahTextStyle.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize),
            )
        },
    )
}

@Composable
private fun SurahNumberBadge(number: Int) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
