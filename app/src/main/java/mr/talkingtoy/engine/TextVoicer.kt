package mr.talkingtoy.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

object TextVoicer {
    private var tts: TextToSpeech? = null

    fun voiceText(context: Context, callback: ()-> Unit, phrase: String) {

            tts = createTTSInstance(context, callback) { tts ->
                tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "11")
            }
            tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "11")

    }


    fun createTTSInstance(
        context: Context,
        callback: ()-> Unit,
        onInstantiateCallback: (TextToSpeech?) -> Unit
    ): TextToSpeech {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TEST5", "SUCCESS")
                tts?.language = Locale("ru", "RU")
                val voice = Voice(
                    "ru-ru-x-ruf-network",
                    Locale("ru", "RU"),
                    400,
                    200,
                    true,
                    setOf("networkTimeoutMs", "legacySetLanguageVoice", "networkRetriesCount")
                )
                tts?.voice = voice
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {
                        // Вызывается, когда начинается воспроизведение фразы
                    }

                    override fun onDone(utteranceId: String) {
                        Log.d("TEST5", " callback.invoke()")
                        callback.invoke()
                    }

                    override fun onError(utteranceId: String) {
                        Log.d("TEST5", "TTS ERROR")
                    }
                })
                onInstantiateCallback.invoke(tts)
            } else {
                Log.d("TEST5", "TTS ERROR")
            }

        }
        return tts
    }

}