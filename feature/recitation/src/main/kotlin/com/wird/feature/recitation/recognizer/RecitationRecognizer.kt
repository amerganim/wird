package com.wird.feature.recitation.recognizer

import kotlinx.coroutines.flow.Flow

/**
 * A streaming speech source for follow-along recitation. Unlike the one-shot
 * [com.wird.feature.recitation.asr.AsrEngine] (used for ayah-by-ayah checks), this
 * emits the *cumulative* recognized transcript continuously as the user recites a
 * whole surah, so the tracker can confirm words one by one and stick on a mistake.
 *
 * Collecting [transcripts] starts audio capture + recognition; cancelling the
 * collection stops it. [expectedText] lets a constrained/simulated recognizer use
 * the target as a prior; a plain engine may ignore it.
 */
interface RecitationRecognizer {
    val displayName: String
    val isSimulated: Boolean

    fun transcripts(expectedText: String): Flow<String>
}
