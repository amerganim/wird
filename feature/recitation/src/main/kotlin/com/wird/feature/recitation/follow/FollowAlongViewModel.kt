package com.wird.feature.recitation.follow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.recitation.data.RecitationRepository
import com.wird.feature.recitation.navigation.RecitationDestinations
import com.wird.feature.recitation.recognizer.RecitationRecognizer
import com.wird.feature.recitation.text.RecitationText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One expected word in the surah, with where it lives (for display + mistake logging). */
data class FollowWord(
    val display: String,
    val ayahNo: Int,
    val ayahId: Int,
    val posInAyah: Int,
)

data class FollowAlongUiState(
    val surahName: String = "",
    val words: List<FollowWord> = emptyList(),
    val engineName: String = "",
    val isSimulated: Boolean = true,
    val listening: Boolean = false,
    val cursor: Int = 0,
    val stuck: Boolean = false,
    val stuckHeard: String? = null,
    val complete: Boolean = false,
    val loading: Boolean = true,
) {
    val expectedNextWord: String? get() = words.getOrNull(cursor)?.display
}

@HiltViewModel
class FollowAlongViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RecitationRepository,
    private val recognizer: RecitationRecognizer,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[RecitationDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(
        FollowAlongUiState(engineName = recognizer.displayName, isSimulated = recognizer.isSimulated),
    )
    val state: StateFlow<FollowAlongUiState> = _state.asStateFlow()

    private var tracker: FollowAlongTracker = FollowAlongTracker(emptyList())
    private var expectedText: String = ""
    private val stuckPositions = mutableSetOf<Int>()
    private var listenJob: Job? = null

    init {
        viewModelScope.launch {
            val ayat = repository.getAyat(surahNo)
            val name = repository.getSurahName(surahNo)
            val words = buildList {
                ayat.forEach { ayah ->
                    ayah.textUthmani.trim().split(WHITESPACE)
                        .filter { RecitationText.normalizeWord(it).isNotEmpty() }
                        .forEachIndexed { pos, display ->
                            add(FollowWord(display, ayah.ayahNo, ayah.id, pos))
                        }
                }
            }
            tracker = FollowAlongTracker(words.map { RecitationText.normalizeWord(it.display) })
            expectedText = words.joinToString(" ") { it.display }
            _state.value = _state.value.copy(surahName = name, words = words, loading = false)
        }
    }

    fun toggle() {
        if (_state.value.listening) stop() else start()
    }

    private fun start() {
        if (_state.value.words.isEmpty()) return
        stuckPositions.clear()
        _state.value = _state.value.copy(listening = true, cursor = 0, stuck = false, stuckHeard = null, complete = false)
        listenJob = viewModelScope.launch {
            try {
                recognizer.transcripts(expectedText).collect { transcript ->
                    val recognized = transcript.trim().split(WHITESPACE)
                    val st = tracker.match(recognized)
                    if (st.stuck) stuckPositions.add(st.cursor)
                    val complete = st.isComplete(tracker.size)
                    _state.value = _state.value.copy(
                        cursor = st.cursor,
                        stuck = st.stuck,
                        stuckHeard = st.stuckHeard,
                        complete = complete,
                    )
                    if (complete) return@collect
                }
            } finally {
                _state.value = _state.value.copy(listening = false)
                logStumbles()
            }
        }
    }

    private fun stop() {
        listenJob?.cancel()
        listenJob = null
        _state.value = _state.value.copy(listening = false)
    }

    private suspend fun logStumbles() {
        if (stuckPositions.isEmpty()) return
        val words = _state.value.words
        val entries = stuckPositions.mapNotNull { g -> words.getOrNull(g)?.let { it.ayahId to it.posInAyah } }
        stuckPositions.clear()
        repository.logStumbles(entries)
    }

    override fun onCleared() {
        listenJob?.cancel()
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
