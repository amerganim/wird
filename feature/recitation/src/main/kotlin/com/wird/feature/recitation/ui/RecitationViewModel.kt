package com.wird.feature.recitation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.database.entity.AyahEntity
import com.wird.feature.recitation.align.AlignmentResult
import com.wird.feature.recitation.align.ForcedAligner
import com.wird.feature.recitation.asr.AsrEngine
import com.wird.feature.recitation.data.RecitationRepository
import com.wird.feature.recitation.navigation.RecitationDestinations
import com.wird.feature.recitation.text.RecitationText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One expected ayah word paired with the status the aligner assigned it. */
data class CheckedWord(
    val display: String,
    val status: com.wird.feature.recitation.align.WordStatus,
)

data class RecitationUiState(
    val surahName: String = "",
    val ayat: List<AyahEntity> = emptyList(),
    val index: Int = 0,
    val engineName: String = "",
    val isSimulated: Boolean = true,
    val listening: Boolean = false,
    val checkedWords: List<CheckedWord> = emptyList(),
    val extraWords: List<String> = emptyList(),
    val result: AlignmentResult? = null,
    val loading: Boolean = true,
) {
    val ayah: AyahEntity? get() = ayat.getOrNull(index)
    val hasPrev: Boolean get() = index > 0
    val hasNext: Boolean get() = index < ayat.size - 1
}

@HiltViewModel
class RecitationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RecitationRepository,
    private val engine: AsrEngine,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[RecitationDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(
        RecitationUiState(engineName = engine.displayName, isSimulated = engine.isSimulated),
    )
    val state: StateFlow<RecitationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val ayat = repository.getAyat(surahNo)
            val name = repository.getSurahName(surahNo)
            _state.value = _state.value.copy(surahName = name, ayat = ayat, loading = false)
        }
    }

    fun check() {
        val ayah = _state.value.ayah ?: return
        if (_state.value.listening) return
        viewModelScope.launch {
            _state.value = _state.value.copy(listening = true, result = null, checkedWords = emptyList(), extraWords = emptyList())
            val hypothesis = engine.transcribe(audio = null, sampleRate = SAMPLE_RATE, expectedAyah = ayah.textUthmani)

            // Tokenize the expected ayah while keeping each display word alongside its
            // normalized form, so aligner positions map back onto the shown words.
            val pairs = ayah.textUthmani.trim().split(WHITESPACE)
                .map { it to RecitationText.normalizeWord(it) }
                .filter { it.second.isNotEmpty() }
            val expectedTokens = pairs.map { it.second }
            val heardTokens = RecitationText.tokenize(hypothesis)
            val result = ForcedAligner.align(expectedTokens, heardTokens)

            val checked = result.words.map { CheckedWord(pairs[it.position].first, it.status) }
            repository.logResult(ayah.id, result)
            _state.value = _state.value.copy(
                listening = false,
                result = result,
                checkedWords = checked,
                extraWords = result.extraWords,
            )
        }
    }

    fun next() = move(+1)
    fun prev() = move(-1)

    private fun move(delta: Int) {
        val newIndex = (_state.value.index + delta).coerceIn(0, (_state.value.ayat.size - 1).coerceAtLeast(0))
        if (newIndex == _state.value.index) return
        _state.value = _state.value.copy(
            index = newIndex,
            result = null,
            checkedWords = emptyList(),
            extraWords = emptyList(),
        )
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        val WHITESPACE = Regex("\\s+")
    }
}
