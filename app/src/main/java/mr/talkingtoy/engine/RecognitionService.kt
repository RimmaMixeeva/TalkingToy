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
import mr.talkingtoy.R
import kotlin.random.Random


class RecognitionService : Service() {
    private val binder = MyBinder()
    private lateinit var speechRecognizer: SpeechRecognizer
    private var mp3Service: MP3Service? = null
    private var mp3ServiceIsBounded: Boolean = false
    private var isRepeatingPhrase = false
    private val context = this
    private var applicationContext: Context? = null
    private var recognitionIntent: Intent? = null

    private enum class CASE { STORY, REPEAT, SONG, TALK, STOP }

    private data class RecognitionItem(
        var name: CASE,
        var codeWords: ArrayList<String>,
        var functionality: () -> Unit,
    )

    private var itemsList: ArrayList<RecognitionItem> = arrayListOf(
        RecognitionItem(CASE.STORY, KeyWords.STORY_KEYWORDS) {
            FeatureHandler.mp3FeatureIsOn = true
            TextVoicer.voiceText(context, {
                var resource = SettingsManager.getString(SettingsManager.TALE, "null", applicationContext!!)
                if (resource == "Рандом"){
                    val tales = ResInfo.getTalesChoice().map { item -> item.key }.toList().filter { key -> key != "Рандом" }
                    val randomNumber = Random.nextInt(tales.size)
                    resource = tales[randomNumber]
                } else if (resource == "null"){
                    val tales = ResInfo.getTalesChoice().map { item -> item.key }.toList().filter { key -> key != "Рандом" }
                    resource = tales[0]
                }

                mp3Service?.initPlayer(ResInfo.getTalesChoice()[resource]!!)
                mp3Service?.startPlay()
            }, "Режим сказки")
        },
        RecognitionItem(CASE.REPEAT, KeyWords.REPEAT_KEYWORDS) {
            TextVoicer.voiceText(context, {
                FeatureHandler.repeatFeatureIsOn = true
            }, "Режим повторяшки")
        },
        RecognitionItem(CASE.SONG, KeyWords.MUSIC_KEYWORDS) {
            FeatureHandler.mp3FeatureIsOn = true
            TextVoicer.voiceText(context, {
                var resource = SettingsManager.getString(SettingsManager.SONG, "null", applicationContext!!)
                if (resource == "Рандом"){
                    val songs = ResInfo.getSongsChoice().map { item -> item.key }.toList().filter { key -> key != "Рандом" }
                    val randomNumber = Random.nextInt(songs.size)
                    resource = songs[randomNumber]
                } else if (resource == "null"){
                    val songs = ResInfo.getSongsChoice().map { item -> item.key }.toList().filter { key -> key != "Рандом" }
                    resource = songs[0]
                }

                mp3Service?.initPlayer(ResInfo.getSongsChoice()[resource]!!)
                mp3Service?.startPlay()
            }, "Режим песни")
        },
        RecognitionItem(CASE.TALK, KeyWords.TALK_KEYWORDS) {
            TextVoicer.voiceText(
                context,
                {
                    FeatureHandler.talkFeatureIsOn = true
                    TextVoicer.voiceText(context, {}, TalkHandler.processAnswer("", {FeatureHandler.talkFeatureIsOn = false}))
                },
                "Режим разговора"
            )
        },
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
        applicationContext = this.getApplicationContext()
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognitionIntent?.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognitionIntent?.putExtra(
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
                    if (FeatureHandler.repeatFeatureIsOn && !executeStop(recognizedText)) {
                        speechRecognizer.stopListening()
                        if (!isRepeatingPhrase && recognizedText.trim() != "режим повторяшки") {
                            isRepeatingPhrase = true
                            TextVoicer.voiceText(context, {
                                isRepeatingPhrase = false
                                MainScope().launch {
                                    speechRecognizer.startListening(recognitionIntent)
                                }
                            }, recognizedText)
                        } else {
                            speechRecognizer.startListening(recognitionIntent)
                        }

                    } else if (FeatureHandler.talkFeatureIsOn) {
                        speechRecognizer.stopListening()
                        TextVoicer.voiceText(context, {
                            MainScope().launch {
                                speechRecognizer.startListening(recognitionIntent)
                            }
                        }, TalkHandler.processAnswer(recognizedText.trim()) {
                            FeatureHandler.talkFeatureIsOn = false
                        })
                    } else {
                        for (recognitionItem in itemsList) {
                            var isMatch = false
                            for (item in recognitionItem.codeWords) {
                                val regex = Regex(item, RegexOption.IGNORE_CASE)
                                isMatch = isMatch || regex.containsMatchIn(recognizedText)
                            }
                            if (isMatch) {
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
                executeStop(last)

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
            isRepeatingPhrase = false
        }
    }

    private fun executeStop(str: String): Boolean {
        var isMatch = false
        for (item in KeyWords.STOP_KEYWORDS) {
            val regex = Regex(item, RegexOption.IGNORE_CASE)
            isMatch = isMatch || regex.containsMatchIn(str)
        }
        if (isMatch) {
            if (mp3Service?.isOn() == true) {
                mp3Service?.stopPlay()
            }
            FeatureHandler.stop()
        }
        return isMatch
    }
}

