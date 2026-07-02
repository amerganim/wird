package com.wird.feature.recitation.align

/** What happened to one expected word when we tried to follow the recitation. */
enum class WordStatus {
    /** Recited as expected. */
    CORRECT,

    /** Skipped — we never heard it. */
    MISSED,

    /** We heard a different word in its place. */
    SUBSTITUTED,
}

/** The mistake kinds we log (mirrors [WordStatus] plus inserted words). */
enum class MistakeType { MISSED, SUBSTITUTED, EXTRA }

/** One expected word and how it was recited. [heard] is set for substitutions. */
data class AlignedWord(
    val position: Int,
    val expected: String,
    val status: WordStatus,
    val heard: String? = null,
)

/**
 * The outcome of aligning a heard recitation against the expected ayah words.
 * [words] has one entry per expected word (in order); [extraWords] are words we
 * heard that don't belong to the ayah.
 */
data class AlignmentResult(
    val words: List<AlignedWord>,
    val extraWords: List<String>,
) {
    val correctCount: Int get() = words.count { it.status == WordStatus.CORRECT }
    val missedCount: Int get() = words.count { it.status == WordStatus.MISSED }
    val substitutedCount: Int get() = words.count { it.status == WordStatus.SUBSTITUTED }

    /** Total problems: missed + substituted expected words + extra words heard. */
    val mistakeCount: Int get() = missedCount + substitutedCount + extraWords.size

    val isPerfect: Boolean get() = mistakeCount == 0
}
