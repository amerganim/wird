package com.wird.feature.hifz.audio

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * Pre-fetches per-ayah audio into the shared Media3 cache so tikrar plays with no
 * network afterwards. Uses [CacheWriter] per URL to write each full file into the
 * same [Cache] that playback reads from.
 */
@Singleton
class AudioDownloader @Inject constructor(
    private val cache: Cache,
) {
    @UnstableApi
    private val dataSourceFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())

    /**
     * Downloads every URL into the cache, reporting completed/total after each.
     * Cancellable — respects the calling coroutine's cancellation between files.
     */
    @UnstableApi
    suspend fun download(urls: List<String>, onProgress: (done: Int, total: Int) -> Unit) =
        withContext(Dispatchers.IO) {
            urls.forEachIndexed { index, url ->
                coroutineContext.ensureActive()
                val dataSpec = DataSpec(Uri.parse(url))
                val dataSource = dataSourceFactory.createDataSource()
                CacheWriter(dataSource, dataSpec, null, null).cache()
                onProgress(index + 1, urls.size)
            }
        }
}
