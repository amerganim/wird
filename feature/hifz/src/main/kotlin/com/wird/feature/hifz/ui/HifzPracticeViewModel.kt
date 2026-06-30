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

/** How much of each ayah is hidden during practice. */
enum class BlankLevel(val label: String) {
    FULL("Full"),
    SOME("Hide ¼"),
    MOST("Hide ½"),
    FIRST_LETTERS("First letters"),
}

data class PracticeUiState(
    val surahName: String = "",
    val ayat: List<AyahEntity> = emptyList(),
    val level: BlankLevel = BlankLevel.FULL,
    val loading: Boolean = true,
)

@HiltViewModel
class HifzPracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HifzRepository,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[HifzDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val ayat = repository.getMemorizedAyat(surahNo)
            val name = repository.getSurahName(surahNo)
            _state.value = PracticeUiState(surahName = name, ayat = ayat, loading = false)
        }
    }

    fun setLevel(level: BlankLevel) {
        _state.value = _state.value.copy(level = level)
    }
}
