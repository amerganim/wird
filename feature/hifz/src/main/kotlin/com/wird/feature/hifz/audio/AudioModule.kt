package com.wird.feature.hifz.audio

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * A single, process-wide [SimpleCache] for ayah audio. Streamed bytes are written
 * here on first play so tikrar loops (and later replays) work offline. SimpleCache
 * must be a singleton — only one instance may own a cache directory at a time.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    private const val MAX_CACHE_BYTES = 256L * 1024 * 1024 // 256 MB

    @Provides
    @Singleton
    @UnstableApi
    fun provideAudioCache(@ApplicationContext context: Context): Cache =
        SimpleCache(
            File(context.cacheDir, "ayah_audio"),
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
            StandaloneDatabaseProvider(context),
        )
}
