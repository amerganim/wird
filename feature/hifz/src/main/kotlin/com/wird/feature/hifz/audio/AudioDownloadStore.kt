package com.wird.feature.hifz.audio

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.audioDownloadStore by preferencesDataStore(name = "audio_downloads")

/** Remembers which surahs have been fully pre-downloaded for offline tikrar. */
@Singleton
class AudioDownloadStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadedKey = stringSetPreferencesKey("downloaded_surahs")

    val downloadedSurahs: Flow<Set<Int>> = context.audioDownloadStore.data.map { prefs ->
        prefs[downloadedKey].orEmpty().mapNotNull(String::toIntOrNull).toSet()
    }

    suspend fun markDownloaded(surahNo: Int) {
        context.audioDownloadStore.edit { prefs ->
            prefs[downloadedKey] = prefs[downloadedKey].orEmpty() + surahNo.toString()
        }
    }
}
