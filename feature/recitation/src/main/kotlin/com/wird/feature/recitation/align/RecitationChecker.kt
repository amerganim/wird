package com.wird.feature.recitation.align

import com.wird.feature.recitation.text.RecitationText

/**
 * Checks a heard recitation against an expected ayah: normalizes and tokenizes both,
 * then forced-aligns them. The result's word positions index the expected ayah's
 * word list (after tokenization), so the UI can highlight the original words.
 */
object RecitationChecker {

    fun check(expectedAyah: String, heard: String): AlignmentResult {
        val expectedTokens = RecitationText.tokenize(expectedAyah)
        val heardTokens = RecitationText.tokenize(heard)
        return ForcedAligner.align(expectedTokens, heardTokens)
    }
}
