package com.wird.feature.recitation.align

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end: normalization + tokenization + alignment on real Uthmani ayat. */
class RecitationCheckerTest {

    private val bismillah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

    @Test
    fun bareRecitationMatchesUthmaniWithDiacritics() {
        val r = RecitationChecker.check(bismillah, "بسم الله الرحمن الرحيم")
        assertTrue(r.isPerfect)
        assertEquals(4, r.correctCount)
    }

    @Test
    fun differentlyDiacritizedRecitationStillMatches() {
        val r = RecitationChecker.check(bismillah, "بِسم اللَّه الرَّحمٰن الرَّحيم")
        assertTrue(r.isPerfect)
    }

    @Test
    fun flagsASkippedWord() {
        val r = RecitationChecker.check(bismillah, "بسم الرحمن الرحيم")
        assertEquals(1, r.missedCount)
        assertEquals("الله", r.words.first { it.status == WordStatus.MISSED }.expected)
    }

    @Test
    fun flagsASubstitutedWord() {
        val r = RecitationChecker.check(bismillah, "بسم الله النور الرحيم")
        assertEquals(1, r.substitutedCount)
        val sub = r.words.first { it.status == WordStatus.SUBSTITUTED }
        assertEquals("الرحمن", sub.expected)
        assertEquals("النور", sub.heard)
    }

    @Test
    fun flagsAnExtraWord() {
        val r = RecitationChecker.check(bismillah, "بسم الله الرحمن الرحيم امين")
        assertEquals(listOf("امين"), r.extraWords)
        assertEquals(4, r.correctCount)
    }
}
