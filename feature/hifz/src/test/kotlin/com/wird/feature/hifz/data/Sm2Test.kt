package com.wird.feature.hifz.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sm2Test {

    @Test
    fun `first Good review sets interval 1 and repetition 1, ease unchanged`() {
        val state = Sm2.review(Sm2.NEW, Sm2.Grade.GOOD)
        assertEquals(1, state.intervalDays)
        assertEquals(1, state.repetitions)
        assertEquals(2.5, state.easeFactor, 1e-9)
        assertEquals(0, state.lapses)
    }

    @Test
    fun `second Good review interval becomes 6`() {
        val first = Sm2.review(Sm2.NEW, Sm2.Grade.GOOD)
        val second = Sm2.review(first, Sm2.Grade.GOOD)
        assertEquals(6, second.intervalDays)
        assertEquals(2, second.repetitions)
    }

    @Test
    fun `third Good review multiplies interval by ease`() {
        var state = Sm2.review(Sm2.NEW, Sm2.Grade.GOOD) // interval 1
        state = Sm2.review(state, Sm2.Grade.GOOD) // interval 6
        state = Sm2.review(state, Sm2.Grade.GOOD) // 6 * 2.5 = 15
        assertEquals(15, state.intervalDays)
        assertEquals(3, state.repetitions)
    }

    @Test
    fun `Again resets repetitions, schedules tomorrow, drops ease, increments lapses`() {
        val learned = Sm2.review(Sm2.review(Sm2.NEW, Sm2.Grade.GOOD), Sm2.Grade.GOOD)
        val lapsed = Sm2.review(learned, Sm2.Grade.AGAIN)
        assertEquals(0, lapsed.repetitions)
        assertEquals(1, lapsed.intervalDays)
        assertEquals(1, lapsed.lapses)
        assertTrue(lapsed.easeFactor < learned.easeFactor)
    }

    @Test
    fun `Easy raises ease, Hard lowers ease`() {
        val easy = Sm2.review(Sm2.NEW, Sm2.Grade.EASY)
        val hard = Sm2.review(Sm2.NEW, Sm2.Grade.HARD)
        assertTrue(easy.easeFactor > 2.5)
        assertTrue(hard.easeFactor < 2.5)
    }

    @Test
    fun `ease never drops below 1_3`() {
        var state = Sm2.NEW
        repeat(20) { state = Sm2.review(state, Sm2.Grade.AGAIN) }
        assertTrue(state.easeFactor >= 1.3)
    }
}
