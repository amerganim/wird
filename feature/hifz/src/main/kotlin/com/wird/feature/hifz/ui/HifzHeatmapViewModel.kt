package com.wird.feature.hifz.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.AyahEntity
import com.wird.feature.hifz.data.HifzRepository
import com.wird.feature.hifz.navigation.HifzDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HeatmapUiState(
    val surahName: String = "",
    val ayat: List<AyahEntity> = emptyList(),
    /** ayahId -> mistake count. */
    val mistakes: Map<Int, Int> = emptyMap(),
    val maxMistakes: Int = 0,
    val totalMistakes: Int = 0,
    val loading: Boolean = true,
)

@HiltViewModel
class HifzHeatmapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HifzRepository,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[HifzDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(HeatmapUiState())
    val state: StateFlow<HeatmapUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val ayat = repository.getMemorizedAyat(surahNo)
            val name = repository.getSurahName(surahNo)
            _state.value = _state.value.copy(surahName = name, ayat = ayat, loading = false)
        }
        viewModelScope.launch {
            repository.observeSurahMistakes(surahNo).collect { mistakes ->
                _state.value = _state.value.copy(
                    mistakes = mistakes,
                    maxMistakes = mistakes.values.maxOrNull() ?: 0,
                    totalMistakes = mistakes.values.sum(),
                )
            }
        }
    }
}
