package com.wird.feature.recitation.asr

/**
 * Speech-to-text seam for recitation. Kept deliberately small so a real recognizer
 * (a Quran-tuned on-device model, or a cloud engine behind explicit opt-in) can drop
 * in without touching the alignment pipeline. A plain Arabic model (e.g. Vosk MGB-2)
 * proved unusable on Quranic recitation, so no real engine is wired yet.
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
