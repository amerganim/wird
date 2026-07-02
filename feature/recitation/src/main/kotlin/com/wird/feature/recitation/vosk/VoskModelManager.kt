package com.wird.feature.recitation.vosk

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.vosk.Model
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ModelState {
    data object NotDownloaded : ModelState
    data class Downloading(val fraction: Float) : ModelState
    data object Ready : ModelState
    data class Failed(val message: String) : ModelState
}

/**
 * Downloads and holds the offline Arabic Vosk model. It's fetched on demand (not
 * bundled) to keep the APK small, then lives in filesDir and works fully offline.
 *
 * NOTE: the smallest Arabic Vosk model (MGB-2) is ~300 MB and is trained on
 * broadcast MSA, not Quranic recitation — so real-world accuracy on tajwid is
 * unvalidated and must be measured on-device (the Phase 5 WER gate).
 */
@Singleton
class VoskModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val modelDir = File(context.filesDir, MODEL_DIR)

    @Volatile
    private var model: Model? = null

    private val _state = MutableStateFlow<ModelState>(
        if (isInstalled()) ModelState.Ready else ModelState.NotDownloaded,
    )
    val state: StateFlow<ModelState> = _state.asStateFlow()

    fun isInstalled(): Boolean = File(modelDir, "conf").isDirectory

    /** Loads (once) and returns the model, or null if it isn't installed. */
    suspend fun loadedModel(): Model? = withContext(Dispatchers.IO) {
        if (!isInstalled()) return@withContext null
        model ?: runCatching { Model(modelDir.absolutePath) }.getOrNull()?.also { model = it }
    }

    suspend fun download() = withContext(Dispatchers.IO) {
        if (isInstalled()) {
            _state.value = ModelState.Ready
            return@withContext
        }
        val tmp = File(context.cacheDir, "vosk-ar.zip")
        try {
            _state.value = ModelState.Downloading(0f)
            val conn = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply { connectTimeout = 30_000 }
            conn.connect()
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                tmp.outputStream().use { out ->
                    val buf = ByteArray(1 shl 16)
                    var readTotal = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        readTotal += n
                        if (total > 0) _state.value = ModelState.Downloading((readTotal.toFloat() / total) * 0.9f)
                    }
                }
            }
            unzipStrippingTopDir(tmp, modelDir)
            _state.value = if (isInstalled()) ModelState.Ready else ModelState.Failed("Model unpack failed")
        } catch (e: Exception) {
            _state.value = ModelState.Failed(e.message ?: "Download failed")
        } finally {
            tmp.delete()
        }
    }

    /** Vosk zips wrap everything in a top-level folder; drop it so [modelDir] holds conf/, am/, … */
    private fun unzipStrippingTopDir(zip: File, dest: File) {
        dest.mkdirs()
        ZipInputStream(zip.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val relative = entry.name.substringAfter('/')
                if (relative.isNotEmpty()) {
                    val outFile = File(dest, relative)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { zis.copyTo(it) }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private companion object {
        const val MODEL_DIR = "vosk-ar-model"
        // Offline Arabic model (~300 MB). Downloaded once, then used with no network.
        const val MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-ar-mgb2-0.4.zip"
    }
}
