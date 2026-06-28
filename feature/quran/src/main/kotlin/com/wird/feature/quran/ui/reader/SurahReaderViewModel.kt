package com.wird.feature.quran.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.SurahEntity
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

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Content(
        val surah: SurahEntity,
        val ayahs: List<AyahEntity>,
    ) : ReaderUiState
}

@HiltViewModel
class SurahReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[QuranDestinations.SURAH_NO_ARG])

    val uiState: StateFlow<ReaderUiState> =
        combine(
            flow { emit(repository.getSurah(surahNo)) },
            repository.observeAyahs(surahNo),
        ) { surah, ayahs ->
            if (surah == null) {
                ReaderUiState.Loading
            } else {
                ReaderUiState.Content(surah, ayahs)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReaderUiState.Loading,
        )

    init {
        // Mark this surah as the resume point (top of surah for now).
        viewModelScope.launch {
            repository.ensureSeeded()
            val firstAyah = repository.observeAyahs(surahNo).first().firstOrNull()
            firstAyah?.let { repository.saveLastPosition(it.id) }
        }
    }
}
