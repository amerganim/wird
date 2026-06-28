package com.wird.feature.quran.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.SurahEntity
import com.wird.feature.quran.data.JuzStart
import com.wird.feature.quran.data.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed interface SurahListUiState {
    data object Loading : SurahListUiState
    data class Content(
        val surahs: List<SurahEntity>,
        val juzStarts: List<JuzStart>,
        val continueReading: ContinueReading?,
    ) : SurahListUiState
}

@HiltViewModel
class SurahListViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    val uiState: StateFlow<SurahListUiState> = flow {
        repository.ensureSeeded()
        val juzStarts = repository.getJuzStarts()
        emitAll(
            combine(
                repository.observeSurahs(),
                repository.observeLastPosition(),
            ) { surahs, last -> surahs to last }
                .map { (surahs, last) ->
                    SurahListUiState.Content(
                        surahs = surahs,
                        juzStarts = juzStarts,
                        continueReading = last?.let { resolveContinue(it.ayahId) },
                    )
                },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SurahListUiState.Loading,
    )

    private suspend fun resolveContinue(ayahId: Int): ContinueReading? {
        val ayah = repository.getAyah(ayahId) ?: return null
        val surah = repository.getSurah(ayah.surahNo) ?: return null
        return ContinueReading(surah.number, surah.nameTranslit, ayah.ayahNo)
    }
}
