package com.wird.feature.quran.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.SurahEntity
import com.wird.feature.quran.data.JuzStart
import com.wird.feature.quran.data.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ContinueReading(
    val surahNumber: Int,
    val surahNameTranslit: String,
    val ayahNo: Int,
)

data class BookmarkListItem(
    val ayahId: Int,
    val surahNo: Int,
    val surahNameTranslit: String,
    val ayahNo: Int,
    val snippet: String,
)

sealed interface SurahListUiState {
    data object Loading : SurahListUiState
    data class Content(
        val surahs: List<SurahEntity>,
        val juzStarts: List<JuzStart>,
        val continueReading: ContinueReading?,
        val bookmarks: List<BookmarkListItem>,
        val query: String = "",
    ) : SurahListUiState
}

@HiltViewModel
class SurahListViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SurahListUiState> = flow {
        repository.ensureSeeded()
        val juzStarts = repository.getJuzStarts()
        val surahMap = repository.getSurahMap()
        emitAll(
            combine(
                repository.observeSurahs(),
                repository.observeLastPosition(),
                query,
                repository.observeBookmarks(),
            ) { surahs, last, q, bookmarks ->
                val trimmed = q.trim()
                val bookmarkItems = bookmarks.mapNotNull { bm ->
                    val ayah = repository.getAyah(bm.ayahId) ?: return@mapNotNull null
                    val surah = surahMap[ayah.surahNo] ?: return@mapNotNull null
                    BookmarkListItem(
                        ayahId = bm.ayahId,
                        surahNo = surah.number,
                        surahNameTranslit = surah.nameTranslit,
                        ayahNo = ayah.ayahNo,
                        snippet = ayah.textUthmani.take(70),
                    )
                }
                SurahListUiState.Content(
                    surahs = if (trimmed.isEmpty()) surahs else surahs.filter { it.matches(trimmed) },
                    juzStarts = juzStarts,
                    // Hide the resume card while actively searching.
                    continueReading = if (trimmed.isEmpty()) last?.let { resolveContinue(it.ayahId) } else null,
                    bookmarks = bookmarkItems,
                    query = q,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SurahListUiState.Loading,
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    private fun SurahEntity.matches(q: String): Boolean =
        nameTranslit.contains(q, ignoreCase = true) ||
            nameEn.contains(q, ignoreCase = true) ||
            nameAr.contains(q) ||
            number.toString() == q

    private suspend fun resolveContinue(ayahId: Int): ContinueReading? {
        val ayah = repository.getAyah(ayahId) ?: return null
        val surah = repository.getSurah(ayah.surahNo) ?: return null
        return ContinueReading(surah.number, surah.nameTranslit, ayah.ayahNo)
    }
}
