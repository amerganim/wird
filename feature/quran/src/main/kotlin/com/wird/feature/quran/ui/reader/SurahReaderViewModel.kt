package com.wird.feature.quran.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.quran.data.QuranRepository
import com.wird.feature.quran.data.ReaderSettings
import com.wird.feature.quran.navigation.QuranDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurahReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
    private val readerSettings: ReaderSettings,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[QuranDestinations.SURAH_NO_ARG])

    val uiState: StateFlow<ReaderUiState> = flow {
        repository.ensureSeeded()
        val surah = repository.getSurah(surahNo)
        if (surah == null) {
            emit(ReaderUiState.Loading)
            return@flow
        }
        // Resolve the saved resume point once — only restore if it's in this surah.
        val saved = repository.observeLastPosition().first()?.ayahId
        val restoreTo = saved?.takeIf { repository.getAyah(it)?.surahNo == surahNo }

        emitAll(
            combine(
                repository.observeAyahs(surahNo),
                readerSettings.arabicFontSp,
            ) { ayahs, fontSp ->
                val items = buildList {
                    if (shouldShowBismillah(surah.number, ayahNo = 1)) {
                        add(ReaderItem.Bismillah(surah.number))
                    }
                    ayahs.forEach { add(ReaderItem.AyahLine(it)) }
                }
                ReaderUiState.Content(
                    title = surah.nameTranslit,
                    items = items,
                    restoreToAyahId = restoreTo,
                    arabicFontSp = fontSp,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReaderUiState.Loading,
    )

    fun onVisibleAyahChanged(ayahId: Int) {
        viewModelScope.launch { repository.saveLastPosition(ayahId) }
    }

    fun onFontSizeChange(sp: Int) {
        viewModelScope.launch { readerSettings.setArabicFontSp(sp) }
    }
}
