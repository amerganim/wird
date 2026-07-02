package com.wird.feature.recitation.align

/**
 * Aligns a heard word sequence against the expected ayah words using
 * Needleman–Wunsch edit-distance alignment (match 0, substitute/insert/delete 1),
 * then backtraces to classify each expected word as CORRECT / MISSED / SUBSTITUTED
 * and collect EXTRA (inserted) words.
 *
 * This is the "forced alignment against a known transcript" that makes recitation
 * mistake-detection tractable: we already know the target text, so we only need to
 * see where the recitation diverges from it — not open-vocabulary recognition.
 */
object ForcedAligner {

    fun align(expected: List<String>, heard: List<String>): AlignmentResult {
        val n = expected.size
        val m = heard.size

        // dp[i][j] = min edits to align expected[0..i) with heard[0..j)
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) dp[i][0] = i
        for (j in 0..m) dp[0][j] = j
        for (i in 1..n) {
            for (j in 1..m) {
                val subCost = if (expected[i - 1] == heard[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j - 1] + subCost, // match / substitute
                    dp[i - 1][j] + 1,           // deletion  (expected word missed)
                    dp[i][j - 1] + 1,           // insertion (extra heard word)
                )
            }
        }

        // Backtrace from (n, m). Prefer the diagonal so matches/substitutions win ties,
        // which keeps the alignment against the expected sequence natural.
        val aligned = ArrayDeque<AlignedWord>()
        val extras = ArrayDeque<String>()
        var i = n
        var j = m
        while (i > 0 || j > 0) {
            val subCost = if (i > 0 && j > 0 && expected[i - 1] == heard[j - 1]) 0 else 1
            when {
                i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + subCost -> {
                    aligned.addFirst(
                        if (subCost == 0) {
                            AlignedWord(i - 1, expected[i - 1], WordStatus.CORRECT)
                        } else {
                            AlignedWord(i - 1, expected[i - 1], WordStatus.SUBSTITUTED, heard[j - 1])
                        },
                    )
                    i--; j--
                }
                i > 0 && dp[i][j] == dp[i - 1][j] + 1 -> {
                    aligned.addFirst(AlignedWord(i - 1, expected[i - 1], WordStatus.MISSED))
                    i--
                }
                else -> { // j > 0: an inserted word that isn't in the ayah
                    extras.addFirst(heard[j - 1])
                    j--
                }
            }
        }

        return AlignmentResult(words = aligned.toList(), extraWords = extras.toList())
    }
}
