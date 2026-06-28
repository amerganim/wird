package com.wird.data.audio

/**
 * Manages per-ayah reciter audio: download, cache, and lookup. Backed by
 * Media3/ExoPlayer for tikrar A–B looping (DEV_PLAN.md §4 / Phase 4). Skeleton
 * for now — implementation lands with the Hifz tikrar engine.
 */
interface ReciterAudioRepository {
    fun audioUriForAyah(reciterId: String, ayahId: Int): String
}
