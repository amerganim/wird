package com.wird.feature.recitation.recognizer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scripts a plausible recitation stream from the expected text so the follow-along
 * UX (word-by-word confirmation, stick-on-mistake) can be exercised without a real
 * microphone or model. It "recites" the surah one word at a time and, once, says a
 * wrong word before self-correcting — so the sticky red state is demonstrable.
 */
@Singleton
class SimulatedRecitationRecognizer @Inject constructor() : RecitationRecognizer {

    override val displayName = "Simulated recitation (prototype)"
    override val isSimulated = true

    override fun transcripts(expectedText: String): Flow<String> = flow {
        val words = expectedText.trim().split(WHITESPACE).filter { it.isNotBlank() }
        if (words.isEmpty()) return@flow
        val errorAt = words.size / 2
        val heard = mutableListOf<String>()
        words.forEachIndexed { i, word ->
            if (i == errorAt) {
                // Stumble: recite a wrong word first — the tracker sticks here…
                emit((heard + WRONG_WORD).joinToString(" "))
                delay(1400)
            }
            // …then say the correct word, which clears the stick and advances.
            heard.add(word)
            emit(heard.joinToString(" "))
            delay(650)
        }
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val WRONG_WORD = "خَطَأ"
    }
}
