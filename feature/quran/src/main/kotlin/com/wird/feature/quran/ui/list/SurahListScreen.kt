package com.wird.feature.quran.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.database.entity.SurahEntity
import com.wird.core.ui.theme.ArabicAyahTextStyle
import com.wird.feature.quran.data.JuzStart

@Composable
fun SurahListRoute(
    onSurahClick: (Int) -> Unit,
    onJuzClick: (Int) -> Unit,
    onBookmarkClick: (Int, Int) -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenDaily: () -> Unit,
    viewModel: SurahListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SurahListScreen(
        uiState = uiState,
        onSurahClick = onSurahClick,
        onJuzClick = onJuzClick,
        onBookmarkClick = onBookmarkClick,
        onOpenKhatm = onOpenKhatm,
        onOpenDaily = onOpenDaily,
        onQueryChange = viewModel::onQueryChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    uiState: SurahListUiState,
    onSurahClick: (Int) -> Unit,
    onJuzClick: (Int) -> Unit,
    onBookmarkClick: (Int, Int) -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenDaily: () -> Unit,
    onQueryChange: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wird") },
                actions = {
                    IconButton(onClick = onOpenDaily) {
                        Icon(Icons.Default.WbSunny, contentDescription = "Daily ayat")
                    }
                    IconButton(onClick = onOpenKhatm) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Khatm plan")
                    }
                },
            )
        },
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

            is SurahListUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Surah") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Juz") },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Saved") },
                    )
                }
                when (selectedTab) {
                    0 -> SurahList(uiState, onSurahClick, onQueryChange, Modifier.weight(1f))
                    1 -> JuzList(uiState.juzStarts, onJuzClick, Modifier.weight(1f))
                    else -> SavedList(uiState.bookmarks, onBookmarkClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SurahList(
    content: SurahListUiState.Content,
    onSurahClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            OutlinedTextField(
                value = content.query,
                onValueChange = onQueryChange,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search surah") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        content.continueReading?.let { cont ->
            item {
                ContinueReadingCard(
                    continueReading = cont,
                    onClick = { onSurahClick(cont.surahNumber) },
                )
            }
        }
        items(content.surahs, key = { it.number }) { surah ->
            SurahRow(surah = surah, onClick = { onSurahClick(surah.number) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun JuzList(
    juzStarts: List<JuzStart>,
    onJuzClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(juzStarts, key = { it.juz }) { juz ->
            ListItem(
                modifier = Modifier.clickable { onJuzClick(juz.juz) },
                leadingContent = { NumberBadge(juz.juz) },
                headlineContent = { Text("Juz ${juz.juz}") },
                supportingContent = {
                    Text("Starts at ${juz.surahNameTranslit} · ayah ${juz.ayahNo}")
                },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun SavedList(
    bookmarks: List<BookmarkListItem>,
    onBookmarkClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bookmarks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No bookmarks yet.\nTap the bookmark icon on an ayah to save it.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp),
            )
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(bookmarks, key = { it.ayahId }) { bm ->
            ListItem(
                modifier = Modifier.clickable { onBookmarkClick(bm.surahNo, bm.ayahId) },
                overlineContent = { Text("${bm.surahNameTranslit} · ayah ${bm.ayahNo}") },
                headlineContent = {
                    Text(
                        text = bm.snippet,
                        style = ArabicAyahTextStyle.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        ),
                        maxLines = 1,
                    )
                },
            )
            HorizontalDivider()
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
        leadingContent = { NumberBadge(surah.number) },
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
private fun NumberBadge(number: Int) {
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
