package com.wird.feature.hifz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun TikrarRoute(
    onBack: () -> Unit,
    viewModel: TikrarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TikrarScreen(
        state = state,
        onBack = onBack,
        onPlayPause = viewModel::playPause,
        onRepeatChange = viewModel::setRepeatEach,
        onSpeedChange = viewModel::setSpeed,
        onAyahClick = viewModel::jumpTo,
        onDownload = viewModel::downloadForOffline,
    )
}

private val SPEEDS = listOf(0.75f, 1f, 1.25f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TikrarScreen(
    state: TikrarUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onRepeatChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onAyahClick: (Int) -> Unit,
    onDownload: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.surahName} · Tikrar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    DownloadAction(
                        status = state.download,
                        progress = state.downloadProgress,
                        onDownload = onDownload,
                    )
                },
            )
        },
        bottomBar = {
            TikrarControls(
                state = state,
                onPlayPause = onPlayPause,
                onRepeatChange = onRepeatChange,
                onSpeedChange = onSpeedChange,
            )
        },
    ) { innerPadding ->
        if (state.loading) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(Modifier.padding(innerPadding).fillMaxSize()) {
                items(state.ayat, key = { it.id }) { ayah ->
                    TikrarAyah(
                        ayah = ayah,
                        isCurrent = ayah.id == state.currentAyahId,
                        onClick = { onAyahClick(ayah.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DownloadAction(status: DownloadStatus, progress: Float, onDownload: () -> Unit) {
    when (status) {
        DownloadStatus.NONE -> IconButton(onClick = onDownload) {
            Icon(Icons.Default.Download, contentDescription = "Download for offline")
        }
        DownloadStatus.DOWNLOADING -> Box(
            Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        }
        DownloadStatus.DONE -> Box(
            Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CloudDone,
                contentDescription = "Downloaded for offline",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TikrarAyah(ayah: AyahEntity, isCurrent: Boolean, onClick: () -> Unit) {
    val bg = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            "Ayah ${ayah.ayahNo}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = ayah.textUthmani,
                style = ArabicAyahTextStyle,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TikrarControls(
    state: TikrarUiState,
    onPlayPause: () -> Unit,
    onRepeatChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Reciter: ${state.reciter}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(onClick = onPlayPause, modifier = Modifier.size(56.dp)) {
                    if (state.buffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text("Repeat each ayah", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedIconButton(
                            onClick = { onRepeatChange(state.repeatEach - 1) },
                            enabled = state.repeatEach > 1,
                            modifier = Modifier.size(36.dp),
                        ) { Icon(Icons.Default.Remove, contentDescription = "Fewer") }
                        Text(
                            "${state.repeatEach}×",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 14.dp),
                        )
                        OutlinedIconButton(
                            onClick = { onRepeatChange(state.repeatEach + 1) },
                            enabled = state.repeatEach < 10,
                            modifier = Modifier.size(36.dp),
                        ) { Icon(Icons.Default.Add, contentDescription = "More") }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SPEEDS.forEach { speed ->
                    FilterChip(
                        selected = state.speed == speed,
                        onClick = { onSpeedChange(speed) },
                        label = { Text(if (speed == 1f) "1×" else "${speed}×") },
                    )
                }
            }
        }
    }
}
