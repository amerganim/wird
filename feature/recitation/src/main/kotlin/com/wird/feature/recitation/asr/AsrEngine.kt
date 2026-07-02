package com.wird.feature.recitation.asr

/**
 * Speech-to-text seam for recitation. Kept deliberately small so the real on-device
 * engine (Vosk Arabic, pending the accuracy spike + on-device/cloud decision gate)
 * drops in without touching the alignment pipeline.
 *
 * [expectedAyah] is passed through because a constrained recognizer can legitimately
 * use the known transcript as a decoding prior; the simulated engine uses it to
 * synthesize a plausible hypothesis. Real engines consume [audio] (16-bit PCM mono).
 */
interface AsrEngine {
    val displayName: String
    val isOnDevice: Boolean

    /** True while recognition is faked — the UI shows a prototype notice. */
    val isSimulated: Boolean

    suspend fun transcribe(audio: ShortArray?, sampleRate: Int, expectedAyah: String): String
}
