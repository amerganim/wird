package com.wird.feature.recitation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.recitation.align.WordStatus

@Composable
fun RecitationRoute(
    onBack: () -> Unit,
    onOpenFollow: () -> Unit,
    viewModel: RecitationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecitationScreen(
        state = state,
        onBack = onBack,
        onCheck = viewModel::check,
        onPrev = viewModel::prev,
        onNext = viewModel::next,
        onOpenFollow = onOpenFollow,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecitationScreen(
    state: RecitationUiState,
    onBack: () -> Unit,
    onCheck: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenFollow: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.surahName} · Recitation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isSimulated) PrototypeBanner(state.engineName)

            val ayah = state.ayah
            if (state.loading || ayah == null) {
                Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Text(
                "Ayah ${ayah.ayahNo} · ${state.index + 1} of ${state.ayat.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            AyahDisplay(state = state, fullText = ayah.textUthmani)

            Button(
                onClick = onCheck,
                enabled = !state.listening,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.listening) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Mic, contentDescription = null)
                }
                Text(
                    if (state.listening) "  Listening…" else "  Check my recitation",
                )
            }

            state.result?.let { ResultCard(state) }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onPrev, enabled = state.hasPrev, modifier = Modifier.weight(1f)) {
                    Text("Previous")
                }
                OutlinedButton(onClick = onNext, enabled = state.hasNext, modifier = Modifier.weight(1f)) {
                    Text("Next ayah")
                }
            }

            OutlinedButton(onClick = onOpenFollow, modifier = Modifier.fillMaxWidth()) {
                Text("Recite the whole surah (follow along)")
            }
        }
    }
}

@Composable
private fun PrototypeBanner(engineName: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Prototype: recognition is simulated ($engineName). Real on-device recognition " +
                "lands after the accuracy spike.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AyahDisplay(state: RecitationUiState, fullText: String) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (state.checkedWords.isEmpty()) {
            Text(
                text = fullText,
                style = ArabicAyahTextStyle,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.checkedWords.forEach { word ->
                    val color = when (word.status) {
                        WordStatus.CORRECT -> MaterialTheme.colorScheme.onSurface
                        WordStatus.MISSED -> MaterialTheme.colorScheme.error
                        WordStatus.SUBSTITUTED -> MaterialTheme.colorScheme.error
                    }
                    val decoration = when (word.status) {
                        WordStatus.MISSED -> TextDecoration.LineThrough
                        WordStatus.SUBSTITUTED -> TextDecoration.Underline
                        WordStatus.CORRECT -> null
                    }
                    Text(
                        text = word.display,
                        style = ArabicAyahTextStyle,
                        color = color,
                        textDecoration = decoration,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(state: RecitationUiState) {
    val result = state.result ?: return
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (result.isPerfect) {
                Text("Followed along cleanly", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Every word lined up. Still your call — this is a practice aid, not a ruling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val parts = buildList {
                    if (result.missedCount > 0) add("${result.missedCount} skipped")
                    if (result.substitutedCount > 0) add("${result.substitutedCount} different")
                    if (result.extraWords.isNotEmpty()) add("${result.extraWords.size} extra")
                }.joinToString(" · ")
                Text("We couldn't follow along here", style = MaterialTheme.typography.titleMedium)
                Text(parts, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Text(
                    "Highlighted words are where we lost track — double-check yourself. " +
                        "This is a practice aid, not an authority on the recitation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.extraWords.isNotEmpty()) {
                    Text(
                        "Heard extra: ${state.extraWords.joinToString("، ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
