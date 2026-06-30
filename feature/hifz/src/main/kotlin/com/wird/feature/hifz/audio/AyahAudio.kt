package com.wird.feature.hifz.audio

/**
 * Per-ayah recitation audio.
 *
 * Source: everyayah.com, reciter **Mahmoud Khalil Al-Husary** (`Husary_128kbps`) —
 * a slow, measured murattal that is the classic choice for memorization (tikrar).
 * URLs are stable per-ayah MP3s named `{surah3}{ayah3}.mp3`, e.g. `001001.mp3`.
 */
object AyahAudio {
    const val RECITER_NAME = "Mahmoud Khalil Al-Husary"
    private const val BASE = "https://everyayah.com/data/Husary_128kbps"

    fun url(surahNo: Int, ayahNo: Int): String =
        "%s/%03d%03d.mp3".format(BASE, surahNo, ayahNo)
}
