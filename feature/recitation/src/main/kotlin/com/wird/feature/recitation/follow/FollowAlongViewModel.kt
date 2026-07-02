package com.wird.feature.recitation.follow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.recitation.data.RecitationRepository
import com.wird.feature.recitation.navigation.RecitationDestinations
import com.wird.feature.recitation.recognizer.RecitationRecognizer
import com.wird.feature.recitation.recognizer.SimulatedRecitationRecognizer
import com.wird.feature.recitation.text.RecitationText
import com.wird.feature.recitation.vosk.ModelState
import com.wird.feature.recitation.vosk.VoskModelManager
import com.wird.feature.recitation.vosk.VoskRecitationRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val modelState: ModelState = ModelState.NotDownloaded,
    val audioGranted: Boolean = false,
    val listening: Boolean = false,
    val cursor: Int = 0,
    val stuck: Boolean = false,
    val stuckHeard: String? = null,
    val complete: Boolean = false,
    val error: String? = null,
    val loading: Boolean = true,
) {
    val expectedNextWord: String? get() = words.getOrNull(cursor)?.display
    val modelReady: Boolean get() = modelState is ModelState.Ready
    /** Real recognition is used only once the model is present and mic permission granted. */
    val canUseReal: Boolean get() = modelReady && audioGranted
}

@HiltViewModel
class FollowAlongViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RecitationRepository,
    private val simulated: SimulatedRecitationRecognizer,
    private val vosk: VoskRecitationRecognizer,
    private val modelManager: VoskModelManager,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[RecitationDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(FollowAlongUiState())
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
            _state.update { it.copy(surahName = name, words = words, loading = false) }
        }
        viewModelScope.launch {
            modelManager.state.collect { ms -> _state.update { it.copy(modelState = ms) } }
        }
    }

    fun setAudioGranted(granted: Boolean) = _state.update { it.copy(audioGranted = granted) }

    fun downloadModel() {
        // Runs in a foreground service so it survives the app being minimized.
        modelManager.startDownload()
    }

    fun toggle() {
        if (_state.value.listening) stop() else start()
    }

    private fun start() {
        if (_state.value.words.isEmpty()) return
        val recognizer: RecitationRecognizer = if (_state.value.canUseReal) vosk else simulated
        stuckPositions.clear()
        _state.update {
            it.copy(
                listening = true,
                engineName = recognizer.displayName,
                isSimulated = recognizer.isSimulated,
                cursor = 0,
                stuck = false,
                stuckHeard = null,
                complete = false,
                error = null,
            )
        }
        listenJob = viewModelScope.launch {
            try {
                recognizer.transcripts(expectedText).collect { transcript ->
                    val recognized = transcript.trim().split(WHITESPACE)
                    val st = tracker.match(recognized)
                    if (st.stuck) stuckPositions.add(st.cursor)
                    val complete = st.isComplete(tracker.size)
                    _state.update {
                        it.copy(cursor = st.cursor, stuck = st.stuck, stuckHeard = st.stuckHeard, complete = complete)
                    }
                    if (complete) return@collect
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Recognition failed") }
            } finally {
                _state.update { it.copy(listening = false) }
                logStumbles()
            }
        }
    }

    private fun stop() {
        listenJob?.cancel()
        listenJob = null
        _state.update { it.copy(listening = false) }
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
