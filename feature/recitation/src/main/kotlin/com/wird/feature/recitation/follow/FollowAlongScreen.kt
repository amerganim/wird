package com.wird.feature.recitation.follow

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.recitation.vosk.ModelState

@Composable
fun FollowAlongRoute(
    onBack: () -> Unit,
    viewModel: FollowAlongViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val granted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        viewModel.setAudioGranted(granted)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setAudioGranted(granted)
        viewModel.toggle() // start regardless — real if granted, simulated otherwise
    }

    FollowAlongScreen(
        state = state,
        onBack = onBack,
        onToggle = {
            when {
                state.listening -> viewModel.toggle()
                state.modelReady && !state.audioGranted ->
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                else -> viewModel.toggle()
            }
        },
        onDownloadModel = viewModel::downloadModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowAlongScreen(
    state: FollowAlongUiState,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onDownloadModel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.surahName} · Follow along") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onToggle,
                icon = {
                    Icon(
                        if (state.listening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                    )
                },
                text = { Text(if (state.listening) "Stop" else "Start reciting") },
            )
        },
    ) { innerPadding ->
        if (state.loading) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            if (!state.canUseReal) PrototypeBanner()
            ModelCard(state, onDownloadModel)
            StatusBar(state)
            SurahBody(state)
        }
    }
}

@Composable
private fun PrototypeBanner() {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Listening is simulated until the on-device Arabic model is downloaded below.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ModelCard(state: FollowAlongUiState, onDownloadModel: () -> Unit) {
    // Once real recognition is available there's nothing to show.
    if (state.canUseReal) return
    OutlinedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("On-device recognition", style = MaterialTheme.typography.titleSmall)
            when (val ms = state.modelState) {
                ModelState.NotDownloaded -> {
                    Text(
                        "Download the offline Arabic voice model (~300 MB) for real recitation " +
                            "recognition. It runs fully on-device afterwards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onDownloadModel) { Text("Download model") }
                }
                is ModelState.Downloading -> {
                    Text("Downloading… ${(ms.fraction * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { ms.fraction }, modifier = Modifier.fillMaxWidth())
                }
                ModelState.Ready -> Text(
                    "Model ready. Tap start and grant the microphone to recite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is ModelState.Failed -> {
                    Text("Download failed: ${ms.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onDownloadModel) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun StatusBar(state: FollowAlongUiState) {
    val total = state.words.size
    val (title, detail) = when {
        state.complete ->
            "Surah complete — masha’Allah" to "You recited all $total words."
        state.stuck ->
            "Waiting on this word" to "Recite “${state.expectedNextWord.orEmpty()}” to continue." +
                (state.stuckHeard?.let { " Heard: $it" } ?: "")
        state.listening ->
            "Listening…" to "Word ${state.cursor + 1} of $total — recite from the highlighted word."
        else ->
            "Recite the whole surah" to "Tap start and recite from the top. Each word confirms as " +
                "you say it; a wrong word waits until you correct it. Practice aid, not a ruling."
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            detail,
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.stuck) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahBody(state: FollowAlongUiState) {
    val scheme = MaterialTheme.colorScheme
    // Group flat words back into ayat, keeping each word's global index.
    val byAyah = state.words.withIndex().groupBy { it.value.ayahNo }
    val ayahNumbers = byAyah.keys.toList()
    val listState = rememberLazyListState()

    // Keep the word being recited in view.
    val currentAyahNo = state.words.getOrNull(state.cursor.coerceAtMost(state.words.size - 1))?.ayahNo
    LaunchedEffect(currentAyahNo, state.listening) {
        if (state.listening && currentAyahNo != null) {
            val index = ayahNumbers.indexOf(currentAyahNo)
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        byAyah.forEach { (ayahNo, indexed) ->
            item(key = ayahNo) {
                Column {
                    Text(
                        "Ayah $ayahNo",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            indexed.forEach { (globalIndex, word) ->
                                WordChip(
                                    text = word.display,
                                    confirmed = globalIndex < state.cursor,
                                    current = globalIndex == state.cursor,
                                    stuck = globalIndex == state.cursor && state.stuck,
                                    listening = state.listening,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordChip(text: String, confirmed: Boolean, current: Boolean, stuck: Boolean, listening: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val bg = when {
        stuck -> scheme.errorContainer
        current && listening -> scheme.primaryContainer
        else -> Color.Transparent
    }
    val fg = when {
        stuck -> scheme.onErrorContainer
        confirmed -> scheme.primary
        current && listening -> scheme.onPrimaryContainer
        else -> scheme.onSurface
    }
    Text(
        text = text,
        style = ArabicAyahTextStyle,
        color = fg,
        fontWeight = if (current || confirmed) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}
