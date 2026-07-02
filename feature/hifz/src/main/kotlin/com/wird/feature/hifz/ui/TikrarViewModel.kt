package com.wird.feature.hifz.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.wird.core.database.entity.AyahEntity
import com.wird.feature.hifz.audio.AudioDownloadStore
import com.wird.feature.hifz.audio.AudioDownloader
import com.wird.feature.hifz.audio.AyahAudio
import com.wird.feature.hifz.data.HifzRepository
import com.wird.feature.hifz.navigation.HifzDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DownloadStatus { NONE, DOWNLOADING, DONE }

/** Tikrar = repetition. Play a surah's memorized ayat, repeating each N times and looping. */
data class TikrarUiState(
    val surahName: String = "",
    val ayat: List<AyahEntity> = emptyList(),
    val reciter: String = AyahAudio.RECITER_NAME,
    val isPlaying: Boolean = false,
    val buffering: Boolean = false,
    val currentAyahId: Int? = null,
    val repeatEach: Int = 3,
    val speed: Float = 1f,
    val download: DownloadStatus = DownloadStatus.NONE,
    val downloadProgress: Float = 0f,
    val loading: Boolean = true,
)

@UnstableApi
@HiltViewModel
class TikrarViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: HifzRepository,
    private val downloader: AudioDownloader,
    private val downloadStore: AudioDownloadStore,
    cache: Cache,
) : ViewModel() {

    private val surahNo: Int = checkNotNull(savedStateHandle[HifzDestinations.SURAH_NO_ARG])

    private val _state = MutableStateFlow(TikrarUiState())
    val state: StateFlow<TikrarUiState> = _state.asStateFlow()

    private val player: ExoPlayer

    init {
        // Stream over HTTP, writing every byte into the shared on-disk cache so loops
        // and replays don't re-fetch.
        val cacheFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.value = _state.value.copy(isPlaying = isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _state.value = _state.value.copy(buffering = playbackState == Player.STATE_BUFFERING)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        _state.value = _state.value.copy(
                            currentAyahId = mediaItem?.mediaId?.toIntOrNull(),
                        )
                    }
                })
            }

        viewModelScope.launch {
            val ayat = repository.getMemorizedAyat(surahNo)
            val name = repository.getSurahName(surahNo)
            val alreadyDownloaded = downloadStore.downloadedSurahs.first().contains(surahNo)
            _state.value = _state.value.copy(
                surahName = name,
                ayat = ayat,
                loading = false,
                download = if (alreadyDownloaded) DownloadStatus.DONE else DownloadStatus.NONE,
            )
            rebuildPlaylist()
        }
    }

    fun downloadForOffline() {
        if (_state.value.download == DownloadStatus.DOWNLOADING) return
        val urls = _state.value.ayat.map { AyahAudio.url(it.surahNo, it.ayahNo) }
        if (urls.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(download = DownloadStatus.DOWNLOADING, downloadProgress = 0f)
            runCatching {
                downloader.download(urls) { done, total ->
                    _state.value = _state.value.copy(downloadProgress = done.toFloat() / total)
                }
            }.onSuccess {
                downloadStore.markDownloaded(surahNo)
                _state.value = _state.value.copy(download = DownloadStatus.DONE, downloadProgress = 1f)
            }.onFailure {
                // Leave partial files cached; let the user retry.
                _state.value = _state.value.copy(download = DownloadStatus.NONE)
            }
        }
    }

    /** Build a playlist where each ayah appears [TikrarUiState.repeatEach] times in order. */
    private fun rebuildPlaylist(resumeAtAyah: Int? = null) {
        val s = _state.value
        val items = s.ayat.flatMap { ayah ->
            val item = MediaItem.Builder()
                .setUri(AyahAudio.url(ayah.surahNo, ayah.ayahNo))
                .setMediaId(ayah.id.toString())
                .build()
            List(s.repeatEach) { item }
        }
        if (items.isEmpty()) return
        val startIndex = resumeAtAyah
            ?.let { id -> s.ayat.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
            ?.let { it * s.repeatEach }
            ?: 0
        player.setMediaItems(items, startIndex, 0L)
        player.setPlaybackParameters(PlaybackParameters(s.speed))
        player.prepare()
    }

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun setRepeatEach(value: Int) {
        val clamped = value.coerceIn(1, 10)
        if (clamped == _state.value.repeatEach) return
        val wasPlaying = player.isPlaying
        _state.value = _state.value.copy(repeatEach = clamped)
        rebuildPlaylist(resumeAtAyah = _state.value.currentAyahId)
        if (wasPlaying) player.play()
    }

    fun setSpeed(value: Float) {
        _state.value = _state.value.copy(speed = value)
        player.setPlaybackParameters(PlaybackParameters(value))
    }

    fun jumpTo(ayahId: Int) {
        val index = _state.value.ayat.indexOfFirst { it.id == ayahId }
        if (index < 0) return
        player.seekTo(index * _state.value.repeatEach, 0L)
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
