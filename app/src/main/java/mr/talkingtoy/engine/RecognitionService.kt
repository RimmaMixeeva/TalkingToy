package mr.talkingtoy.engine

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mr.talkingtoy.KeyWords
import mr.talkingtoy.R


class RecognitionService : Service() {
    private val binder = MyBinder()
    private lateinit var speechRecognizer: SpeechRecognizer
    private var mp3Service: MP3Service? = null
    private var mp3ServiceIsBounded: Boolean = false
    private var isRepeating = false
    private val context = this

    private enum class CASE { STORY, REPEAT, SONG, TALK, STOP }

    private data class RecognitionItem(
        var name: CASE,
        var codeWords: ArrayList<String>,
        var functionality: () -> Unit,
    )

    private var itemsList: ArrayList<RecognitionItem> = arrayListOf(
        RecognitionItem(CASE.STORY, KeyWords.STORY_KEYWORDS) {
            TextVoicer.voiceText(context, {
                mp3Service?.initPlayer(R.raw.song)
                mp3Service?.startPlay()
                Log.d("TEST5", "story ON")
            }, "Режим сказки")
        },
        RecognitionItem(CASE.REPEAT, KeyWords.REPEAT_KEYWORDS) {
            Log.d("MODE ON", "REPEATING INVOKED")
            TextVoicer.voiceText(context, {
                FeatureHandler.repeatFeatureIsOn = true
            }, "Режим повторяшки")
        },
        RecognitionItem(CASE.SONG, KeyWords.MUSIC_KEYWORDS) {
            TextVoicer.voiceText(context, {
                mp3Service?.initPlayer(R.raw.song)
                mp3Service?.startPlay()}, "Режим песни")
        },
        RecognitionItem(CASE.TALK, KeyWords.TALK_KEYWORDS) { Log.d("TEST5", "TALK ON") },
        RecognitionItem(CASE.STOP, KeyWords.STOP_KEYWORDS) {
            if (mp3Service?.isOn() == true) mp3Service?.stopPlay()
            FeatureHandler.stop()
        }
    )

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognitionIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        registerReceiver(myBroadcastReceiver, IntentFilter(BroadcastMessages.TTS))
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {
            }

            override fun onError(p0: Int) {
                val errorMessage: String = when (p0) {
                    SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудио"
                    SpeechRecognizer.ERROR_CLIENT -> "Ошибка клиента"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Недостаточно разрешений"
                    SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Тайм-аут сети"
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        speechRecognizer.startListening(recognitionIntent)
                        "Нет совпадений"
                    }

                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Распознаватель занят"
                    SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Тайм-аут речи"
                    else -> "Неизвестная ошибка: $p0"
                }
                Log.e("SpeechRecognition", "Ошибка распознавания речи: $errorMessage")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    // Полученные результаты
                    val recognizedText = it[0]
                    if (FeatureHandler.repeatFeatureIsOn) {
                        speechRecognizer.stopListening()
                        if (!isRepeating) {
                            isRepeating = true
                            TextVoicer.voiceText(context, {
                                isRepeating = false
                                MainScope().launch {
                                    speechRecognizer.startListening(recognitionIntent)
                                }
                            }, recognizedText)
                        } else {
                            speechRecognizer.startListening(recognitionIntent)
                        }
                    } else {
                        for (recognitionItem in itemsList) {
                            var isMatch = false
                            for (item in recognitionItem.codeWords) {
                                val regex = Regex(item, RegexOption.IGNORE_CASE)
                                isMatch = isMatch || regex.containsMatchIn(recognizedText)
                            }
                            if (isMatch) {
                                Log.d("MODE ON", recognitionItem.name.toString() + "MODE")
                                recognitionItem.functionality.invoke()
                            }
                        }
                        speechRecognizer.startListening(recognitionIntent)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                var last = matches[0].split(" ").last()
                var isMatch = false
                for (item in KeyWords.STOP_KEYWORDS) {
                    val regex = Regex(item, RegexOption.IGNORE_CASE)
                    isMatch = isMatch || regex.containsMatchIn(last)
                }
                if (isMatch) {
                    if (mp3Service?.isOn() == true) {
                        mp3Service?.stopPlay()
                    }
                    FeatureHandler.stop()
                }

            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                TODO("Not yet implemented")
            }

            // Другие методы RecognitionListener
        })
        speechRecognizer.startListening(recognitionIntent)

        val intent = Intent(this, MP3Service::class.java)
        bindService(intent, mp3ServiceConnection, BIND_AUTO_CREATE)
    }

    inner class MyBinder : Binder() {
        fun getService(): RecognitionService {
            return this@RecognitionService
        }
    }

    private val mp3ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d("TEST5", "CONNECT")
            // Метод вызывается, когда связь с сервисом установлена
            mp3Service = (binder as MP3Service.MyBinder).getService()
            mp3ServiceIsBounded = true;
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mp3Service = null
            mp3ServiceIsBounded = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mp3ServiceConnection)
        unregisterReceiver(myBroadcastReceiver)
    }

    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isRepeating = false
        }
    }
}

