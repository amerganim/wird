package com.wird.feature.hifz.data

import kotlin.math.roundToInt

/** SM-2 spaced-repetition scheduling (pure logic). */
object Sm2 {

    enum class Grade(val q: Int) {
        AGAIN(2),
        HARD(3),
        GOOD(4),
        EASY(5),
    }

    data class State(
        val easeFactor: Double,
        val intervalDays: Int,
        val repetitions: Int,
        val lapses: Int,
    )

    val NEW = State(easeFactor = 2.5, intervalDays = 0, repetitions = 0, lapses = 0)

    fun review(state: State, grade: Grade): State {
        val q = grade.q
        if (q < 3) {
            // Lapse: reset reps, review again tomorrow, drop ease.
            return state.copy(
                easeFactor = (state.easeFactor - 0.2).coerceAtLeast(MIN_EASE),
                intervalDays = 1,
                repetitions = 0,
                lapses = state.lapses + 1,
            )
        }
        val reps = state.repetitions + 1
        val interval = when (reps) {
            1 -> 1
            2 -> 6
            else -> (state.intervalDays * state.easeFactor).roundToInt().coerceAtLeast(1)
        }
        val ease = (state.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
            .coerceAtLeast(MIN_EASE)
        return State(
            easeFactor = ease,
            intervalDays = interval,
            repetitions = reps,
            lapses = state.lapses,
        )
    }

    private const val MIN_EASE = 1.3
}
