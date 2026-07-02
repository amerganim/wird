package com.wird.feature.recitation.vosk

import com.wird.feature.recitation.recognizer.RecitationRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import javax.inject.Inject

/**
 * Real on-device streaming recognizer backed by Vosk. Captures from the mic via
 * Vosk's [SpeechService] (needs RECORD_AUDIO) and emits the cumulative transcript
 * (finalized segments + the live partial) so [com.wird.feature.recitation.follow.FollowAlongTracker]
 * can confirm words as they're recited.
 *
 * Runs fully offline once [VoskModelManager] has the model. Real recitation accuracy
 * is unvalidated (see the model note) and must be measured on-device.
 */
class VoskRecitationRecognizer @Inject constructor(
    private val modelManager: VoskModelManager,
) : RecitationRecognizer {

    override val displayName = "On-device · Vosk (Arabic)"
    override val isSimulated = false

    override fun transcripts(expectedText: String): Flow<String> = callbackFlow {
        val model = modelManager.loadedModel()
            ?: throw IllegalStateException("Vosk model not installed")
        val recognizer = Recognizer(model, SAMPLE_RATE)
        val finalized = StringBuilder()

        val listener = object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                val partial = hypothesis.textField("partial")
                val combined = (finalized.toString() + " " + partial).trim()
                if (combined.isNotEmpty()) trySend(combined)
            }

            override fun onResult(hypothesis: String?) = appendFinal(hypothesis)
            override fun onFinalResult(hypothesis: String?) = appendFinal(hypothesis)

            private fun appendFinal(hypothesis: String?) {
                val text = hypothesis.textField("text")
                if (text.isNotEmpty()) {
                    finalized.append(' ').append(text)
                    trySend(finalized.toString().trim())
                }
            }

            override fun onError(exception: Exception?) {
                close(exception ?: RuntimeException("Vosk recognition error"))
            }

            override fun onTimeout() {}
        }

        val speech = SpeechService(recognizer, SAMPLE_RATE)
        speech.startListening(listener)

        awaitClose {
            speech.stop()
            speech.shutdown()
            recognizer.close()
        }
    }

    private fun String?.textField(key: String): String =
        if (this.isNullOrBlank()) "" else runCatching { JSONObject(this).optString(key) }.getOrDefault("")

    private companion object {
        const val SAMPLE_RATE = 16_000f
    }
}
