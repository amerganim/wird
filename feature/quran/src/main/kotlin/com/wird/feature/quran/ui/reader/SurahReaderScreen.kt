package com.wird.feature.quran.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.SurahEntity
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.quran.data.ReaderSettings
import com.wird.feature.quran.ui.toArabicIndic
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

// End-of-ayah ornament (U+06DD) followed by the ayah number in Arabic-Indic digits.
private const val END_OF_AYAH = '۝'
private const val BISMILLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Content(
        val title: String,
        val items: List<ReaderItem>,
        val restoreToAyahId: Int? = null,
        val arabicFontSp: Int = ReaderSettings.DEFAULT_SP,
    ) : ReaderUiState
}

@Composable
fun SurahReaderRoute(
    onBack: () -> Unit,
    viewModel: SurahReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReaderScreen(
        uiState = uiState,
        onBack = onBack,
        onVisibleAyah = viewModel::onVisibleAyahChanged,
        onFontSizeChange = viewModel::onFontSizeChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    onBack: () -> Unit,
    onVisibleAyah: (Int) -> Unit = {},
    onFontSizeChange: (Int) -> Unit = {},
) {
    var showFontSheet by rememberSaveable { mutableStateOf(false) }
    val fontSp = (uiState as? ReaderUiState.Content)?.arabicFontSp ?: ReaderSettings.DEFAULT_SP

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is ReaderUiState.Content -> uiState.title
                            ReaderUiState.Loading -> ""
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ReaderUiState.Content) {
                        IconButton(onClick = { showFontSheet = true }) {
                            Icon(Icons.Default.FormatSize, contentDescription = "Text size")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (showFontSheet) {
            FontSizeSheet(
                fontSp = fontSp,
                onFontSizeChange = onFontSizeChange,
                onDismiss = { showFontSheet = false },
            )
        }
        when (uiState) {
            ReaderUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is ReaderUiState.Content -> CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                val listState = rememberLazyListState()
                var restored by rememberSaveable { mutableStateOf(false) }

                // Restore once to the saved ayah for this surah/juz.
                LaunchedEffect(uiState.items, uiState.restoreToAyahId) {
                    if (!restored && uiState.restoreToAyahId != null && uiState.items.isNotEmpty()) {
                        val index = uiState.items.indexOfFirst {
                            it is ReaderItem.AyahLine && it.ayah.id == uiState.restoreToAyahId
                        }
                        if (index >= 0) listState.scrollToItem(index)
                        restored = true
                    }
                }

                // Persist the top-most visible ayah as the resume point.
                LaunchedEffect(uiState.items) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .map { uiState.items.getOrNull(it) }
                        .filterIsInstance<ReaderItem.AyahLine>()
                        .map { it.ayah.id }
                        .distinctUntilChanged()
                        .collect(onVisibleAyah)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    items(uiState.items, key = { it.key }) { item ->
                        when (item) {
                            is ReaderItem.SurahHeader -> SurahHeaderRow(item.surah)
                            is ReaderItem.Bismillah -> BismillahHeader(fontSp)
                            is ReaderItem.AyahLine -> {
                                AyahRow(item.ayah, fontSp)
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahHeaderRow(surah: SurahEntity) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "${surah.number}. ${surah.nameAr}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
        )
    }
}

@Composable
private fun BismillahHeader(fontSp: Int) {
    Text(
        text = BISMILLAH,
        style = arabicStyle(fontSp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 16.dp),
    )
}

@Composable
private fun AyahRow(ayah: AyahEntity, fontSp: Int) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = "${ayah.textUthmani} $END_OF_AYAH${ayah.ayahNo.toArabicIndic()}",
            style = arabicStyle(fontSp),
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun arabicStyle(fontSp: Int) =
    ArabicAyahTextStyle.copy(fontSize = fontSp.sp, lineHeight = (fontSp * 1.8f).sp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSizeSheet(
    fontSp: Int,
    onFontSizeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(
                text = "Arabic text size: $fontSp",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = fontSp.toFloat(),
                onValueChange = { onFontSizeChange(it.toInt()) },
                valueRange = ReaderSettings.MIN_SP.toFloat()..ReaderSettings.MAX_SP.toFloat(),
            )
            Text(
                text = BISMILLAH,
                style = arabicStyle(fontSp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
