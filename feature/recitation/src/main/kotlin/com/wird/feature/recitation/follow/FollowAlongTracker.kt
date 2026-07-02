package com.wird.feature.recitation.follow

import com.wird.feature.recitation.text.RecitationText

/** Where the reciter is against the expected words. */
data class FollowAlongState(
    /** Number of words confirmed correct so far (index of the next expected word). */
    val cursor: Int,
    /** True when a word has been heard that isn't the expected next one — blocked here. */
    val stuck: Boolean,
    /** The (raw) wrong word heard while stuck, for display. */
    val stuckHeard: String?,
) {
    fun isComplete(total: Int): Boolean = cursor >= total
}

/**
 * Follows a continuous recitation against the expected word sequence, confirming
 * words strictly in order. A word that isn't the expected next one leaves the
 * cursor **stuck** on that position (marked) — reciting the correct word clears it
 * and advances. This is recompute-from-scratch on every transcript update, so it is
 * robust to a streaming recognizer revising its partial hypothesis.
 */
class FollowAlongTracker(private val expected: List<String>) {

    val size: Int get() = expected.size

    fun match(recognizedWords: List<String>): FollowAlongState {
        var cursor = 0
        var stuck = false
        var stuckHeard: String? = null
        for (raw in recognizedWords) {
            val word = RecitationText.normalizeWord(raw)
            if (word.isEmpty()) continue
            if (cursor >= expected.size) break
            if (word == expected[cursor]) {
                cursor++
                stuck = false
                stuckHeard = null
            } else {
                // A heard word that isn't expected next — stick here and wait.
                stuck = true
                stuckHeard = raw
            }
        }
        return FollowAlongState(cursor, stuck, stuckHeard)
    }
}
