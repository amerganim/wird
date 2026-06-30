package com.wird.feature.hifz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.database.entity.AyahEntity
import com.wird.core.ui.theme.ArabicAyahTextStyle

@Composable
fun HifzPracticeRoute(
    onBack: () -> Unit,
    viewModel: HifzPracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HifzPracticeScreen(state = state, onBack = onBack, onLevelChange = viewModel::setLevel)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HifzPracticeScreen(
    state: PracticeUiState,
    onBack: () -> Unit,
    onLevelChange: (BlankLevel) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.surahName} · Practice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BlankLevel.entries.forEach { level ->
                    FilterChip(
                        selected = state.level == level,
                        onClick = { onLevelChange(level) },
                        label = { Text(level.label) },
                    )
                }
            }
            HorizontalDivider()

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.ayat, key = { it.id }) { ayah ->
                        AyahPractice(ayah = ayah, level = state.level)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AyahPractice(ayah: AyahEntity, level: BlankLevel) {
    val words = remember(ayah.id) { ayah.textUthmani.split(' ').filter { it.isNotBlank() } }
    // Reset peeked words when the level changes.
    val revealed = remember(ayah.id, level) { mutableStateMapOf<Int, Boolean>() }

    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            "Ayah ${ayah.ayahNo}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                words.forEachIndexed { index, word ->
                    val isRevealed = revealed[index] == true
                    val masked = maskWord(word, index, level, isRevealed)
                    Text(
                        text = masked,
                        style = ArabicAyahTextStyle,
                        color = if (masked == word) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = if (masked == word) {
                            Modifier.padding(vertical = 4.dp)
                        } else {
                            Modifier
                                .clickable { revealed[index] = true }
                                .padding(vertical = 4.dp)
                        },
                    )
                }
            }
        }
    }
}

private const val PLACEHOLDER = "ـــ"

private fun maskWord(word: String, index: Int, level: BlankLevel, revealed: Boolean): String {
    if (revealed) return word
    return when (level) {
        BlankLevel.FULL -> word
        BlankLevel.SOME -> if (index % 4 == 3) PLACEHOLDER else word
        BlankLevel.MOST -> if (index % 2 == 1) PLACEHOLDER else word
        BlankLevel.FIRST_LETTERS -> word.take(1) + "ـ"
    }
}
