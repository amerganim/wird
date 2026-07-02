package com.wird.feature.recitation.align

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForcedAlignerTest {

    @Test
    fun perfectMatch() {
        val r = ForcedAligner.align(listOf("a", "b", "c"), listOf("a", "b", "c"))
        assertTrue(r.isPerfect)
        assertEquals(3, r.correctCount)
        assertTrue(r.words.all { it.status == WordStatus.CORRECT })
    }

    @Test
    fun detectsMissedWord() {
        val r = ForcedAligner.align(listOf("a", "b", "c"), listOf("a", "c"))
        assertEquals(1, r.missedCount)
        assertEquals(WordStatus.MISSED, r.words[1].status)
        assertEquals("b", r.words[1].expected)
        assertEquals(WordStatus.CORRECT, r.words[0].status)
        assertEquals(WordStatus.CORRECT, r.words[2].status)
    }

    @Test
    fun detectsSubstitution() {
        val r = ForcedAligner.align(listOf("a", "b", "c"), listOf("a", "x", "c"))
        assertEquals(1, r.substitutedCount)
        assertEquals(WordStatus.SUBSTITUTED, r.words[1].status)
        assertEquals("x", r.words[1].heard)
    }

    @Test
    fun detectsExtraWord() {
        val r = ForcedAligner.align(listOf("a", "b", "c"), listOf("a", "b", "c", "d"))
        assertEquals(listOf("d"), r.extraWords)
        assertEquals(3, r.correctCount)
        assertEquals(1, r.mistakeCount)
    }

    @Test
    fun emptyHeardMarksEverythingMissed() {
        val r = ForcedAligner.align(listOf("a", "b"), emptyList())
        assertEquals(2, r.missedCount)
        assertEquals(2, r.mistakeCount)
    }

    @Test
    fun emptyExpectedMakesAllHeardExtra() {
        val r = ForcedAligner.align(emptyList(), listOf("a"))
        assertTrue(r.words.isEmpty())
        assertEquals(listOf("a"), r.extraWords)
    }

    @Test
    fun keepsExpectedPositionsInOrder() {
        val r = ForcedAligner.align(listOf("a", "b", "c", "d"), listOf("a", "d"))
        assertEquals(listOf(0, 1, 2, 3), r.words.map { it.position })
        assertEquals(2, r.missedCount) // b and c
    }
}
