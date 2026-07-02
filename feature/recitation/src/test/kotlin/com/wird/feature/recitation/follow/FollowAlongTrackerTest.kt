package com.wird.feature.recitation.follow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowAlongTrackerTest {

    private val expected = listOf("بسم", "الله", "الرحمن", "الرحيم")
    private val tracker = FollowAlongTracker(expected)

    @Test
    fun advancesWordByWord() {
        assertEquals(1, tracker.match(listOf("بسم")).cursor)
        assertEquals(2, tracker.match(listOf("بسم", "الله")).cursor)
        assertEquals(3, tracker.match(listOf("بسم", "الله", "الرحمن")).cursor)
    }

    @Test
    fun ignoresDiacriticsWhenMatching() {
        val s = tracker.match(listOf("بِسْمِ", "ٱللَّه"))
        assertEquals(2, s.cursor)
        assertFalse(s.stuck)
    }

    @Test
    fun sticksOnWrongWordWithoutAdvancing() {
        val s = tracker.match(listOf("بسم", "الله", "خطأ"))
        assertEquals(2, s.cursor) // did not pass the 3rd word
        assertTrue(s.stuck)
        assertEquals("خطأ", s.stuckHeard)
    }

    @Test
    fun clearsStickWhenCorrectWordFollows() {
        // wrong word then the right one — should advance and unstick
        val s = tracker.match(listOf("بسم", "الله", "خطأ", "الرحمن"))
        assertEquals(3, s.cursor)
        assertFalse(s.stuck)
        assertNull(s.stuckHeard)
    }

    @Test
    fun completesWholeSequence() {
        val s = tracker.match(expected)
        assertEquals(4, s.cursor)
        assertTrue(s.isComplete(tracker.size))
    }

    @Test
    fun extraTrailingWordsDoNotOverrun() {
        val s = tracker.match(expected + listOf("امين"))
        assertEquals(4, s.cursor)
        assertTrue(s.isComplete(tracker.size))
    }
}
