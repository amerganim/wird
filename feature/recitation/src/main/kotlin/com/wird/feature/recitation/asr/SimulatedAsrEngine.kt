package com.wird.feature.recitation.asr

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A stand-in recognizer for the Phase 5 prototype: instead of listening, it derives a
 * hypothesis from the expected ayah, cycling deterministically through perfect →
 * skipped word → wrong word → extra word on successive calls. This exercises the full
 * capture→align→highlight→log pipeline (and the UX framing) before a real ASR model
 * is chosen at the decision gate.
 */
@Singleton
class SimulatedAsrEngine @Inject constructor() : AsrEngine {

    override val displayName = "Simulated recognizer (prototype)"
    override val isOnDevice = true
    override val isSimulated = true

    private var calls = 0

    override suspend fun transcribe(audio: ShortArray?, sampleRate: Int, expectedAyah: String): String {
        delay(700) // mimic recognition latency
        val words = expectedAyah.trim().split(WHITESPACE).toMutableList()
        if (words.size < 2) return expectedAyah
        val mid = words.size / 2
        return when (calls++ % 4) {
            0 -> expectedAyah                                   // perfect
            1 -> words.also { it.removeAt(mid) }.joinToString(" ") // skipped a word
            2 -> words.also { it[mid] = WRONG_WORD }.joinToString(" ") // wrong word
            else -> (words + EXTRA_WORD).joinToString(" ")       // extra word
        }
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val WRONG_WORD = "ٱلنُّورِ"   // a real Quranic word, but not this one
        const val EXTRA_WORD = "ءَامِين"
    }
}
