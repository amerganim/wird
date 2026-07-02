package com.wird.feature.hifz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HifzHeatmapRoute(
    onBack: () -> Unit,
    viewModel: HifzHeatmapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HifzHeatmapScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HifzHeatmapScreen(state: HeatmapUiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.surahName} · Trouble spots") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.loading) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.size(4.dp))
            Text(
                if (state.totalMistakes == 0) {
                    "No trouble spots yet. When you grade an ayah \"Again\" during review, " +
                        "it lights up here so you can see where recall breaks down."
                } else {
                    "${state.totalMistakes} mistakes across ${state.mistakes.size} " +
                        "ayah${if (state.mistakes.size == 1) "" else "s"}. Brighter = more stumbles."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.maxMistakes > 0) Legend()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.ayat.forEach { ayah ->
                    val count = state.mistakes[ayah.id] ?: 0
                    HeatCell(ayahNo = ayah.ayahNo, count = count, max = state.maxMistakes)
                }
            }
        }
    }
}

@Composable
private fun HeatCell(ayahNo: Int, count: Int, max: Int) {
    val scheme = MaterialTheme.colorScheme
    val frac = if (max <= 0) 0f else count.toFloat() / max
    // More stumbles = more vivid: the error hue at increasing opacity (theme-robust,
    // unlike lerping between error/errorContainer whose lightness order flips by theme).
    val bg = if (count == 0) scheme.surfaceVariant else scheme.error.copy(alpha = 0.4f + 0.6f * frac)
    val fg = if (count == 0) scheme.onSurfaceVariant else scheme.onSurface
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$ayahNo",
                color = fg,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (count > 0) {
                Text("×$count", color = fg, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun Legend() {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("None", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        listOf(
            scheme.surfaceVariant,
            scheme.error.copy(alpha = 0.4f),
            scheme.error.copy(alpha = 0.7f),
            scheme.error,
        ).forEach { c ->
            Box(
                Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(c),
            )
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text("Most", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
    }
}
