package com.wird.feature.quran.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.quran.data.QuranRepository
import com.wird.feature.quran.navigation.QuranDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JuzReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
) : ViewModel() {

    private val juz: Int = checkNotNull(savedStateHandle[QuranDestinations.JUZ_ARG])

    val uiState: StateFlow<ReaderUiState> =
        combine(
            flow { emit(repository.getSurahMap()) },
            repository.observeAyahsByJuz(juz),
        ) { surahMap, ayahs ->
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
            ReaderUiState.Content(title = "Juz $juz", items = items)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReaderUiState.Loading,
        )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            repository.observeAyahsByJuz(juz).first().firstOrNull()?.let {
                repository.saveLastPosition(it.id)
            }
        }
    }
}
