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
class JuzReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
    private val readerSettings: ReaderSettings,
) : ViewModel() {

    private val juz: Int = checkNotNull(savedStateHandle[QuranDestinations.JUZ_ARG])

    val uiState: StateFlow<ReaderUiState> = flow {
        repository.ensureSeeded()
        val surahMap = repository.getSurahMap()
        // Restore only if the saved ayah falls within this juz.
        val saved = repository.observeLastPosition().first()?.ayahId
        val restoreTo = saved?.takeIf { repository.getAyah(it)?.juz == juz }

        emitAll(
            combine(
                repository.observeAyahsByJuz(juz),
                readerSettings.arabicFontSp,
            ) { ayahs, fontSp ->
                val items = buildList {
                    var currentSurahNo = -1
                    ayahs.forEach { ayah ->
                        if (ayah.surahNo != currentSurahNo) {
                            currentSurahNo = ayah.surahNo
                            surahMap[ayah.surahNo]?.let { add(ReaderItem.SurahHeader(it)) }
                            if (shouldShowBismillah(ayah.surahNo, ayah.ayahNo)) {
                                add(ReaderItem.Bismillah(ayah.surahNo))
                            }
                        }
                        add(ReaderItem.AyahLine(ayah))
                    }
                }
                ReaderUiState.Content(
                    title = "Juz $juz",
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
