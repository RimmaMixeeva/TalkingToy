package mr.talkingtoy

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mr.talkingtoy.engine.FeatureHandler
import mr.talkingtoy.engine.RecognitionService
import mr.talkingtoy.engine.ResInfo
import mr.talkingtoy.engine.SettingsManager
import mr.talkingtoy.engine.SoundManager
import mr.talkingtoy.engine.Spinner
import mr.talkingtoy.engine.TextVoicer


class MainActivity : ComponentActivity() {

    private var recognitionService: RecognitionService? = null
    private var recognitionServiceIsBounded: Boolean = false
    private var soundManager: SoundManager? = null
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        context = this.applicationContext
        super.onCreate(savedInstanceState)

        //выключаем звук уведомлений
        soundManager = SoundManager(this)
        soundManager?.mute()


        var name = SettingsManager.getString(SettingsManager.NAME, "", this)
        var startPhrase = if (SettingsManager.getBoolean(SettingsManager.FIRST_ENTERING, true, this)) {
            SettingsManager.putBoolean(SettingsManager.FIRST_ENTERING, false, this)
            "Привет, друг. Меня зовут Бетти. Давай дружить."
        } else {
            "С возвращением, $name. Рада тебя видеть."
        }

        TextVoicer.voiceText(this, {
            val intent = Intent(this, RecognitionService::class.java)
            bindService(intent, recognitionServiceConnection, BIND_AUTO_CREATE)
        }, startPhrase)

        setContent {
            var soundIsOn by remember {
                mutableStateOf(soundManager?.soundIsOn())
            }
            LaunchedEffect(key1 = Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        soundIsOn = soundManager?.soundIsOn()
                        delay(1000)
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Main Image",
                    painter = painterResource(R.drawable.pic2),
                    contentScale = ContentScale.FillBounds
                )
            }

            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                        .border(
                            width = 1.dp,
                            color = if (soundIsOn != false) Color.Red else White,
                            shape = RectangleShape
                        )
                ) {
                    Text(
                        text = "Выключить звук уведомлений", modifier = Modifier
                            .weight(1f)
                            .padding(8.dp), color = Color.White, fontSize = 18.sp
                    )
                    Button(
                        onClick = { soundManager?.mute() },
                        shape = RectangleShape,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text("Выкл.", color = Color.White)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                        .border(
                            width = 1.dp,
                            color = White,
                            shape = RectangleShape
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Песня: ", modifier = Modifier
                            .weight(1f)
                            .padding(8.dp), color = White, fontSize = 18.sp
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        Spinner(
                            items = ResInfo.getSongsChoice().map { item -> item.key }.toList(),
                            property = SettingsManager.SONG,
                            context!!
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                        .border(
                            width = 1.dp,
                            color = White,
                            shape = RectangleShape
                        )
                ) {
                    Text(
                        text = "Сказка:", modifier = Modifier
                            .weight(1f)
                            .padding(8.dp), color = Color.White, fontSize = 18.sp
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Spinner(
                            items = ResInfo.getTalesChoice().map { item -> item.key }.toList(),
                            property = SettingsManager.TALE,
                            context!!
                        )
                    }
                }
            }
        }
    }

    private val recognitionServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            recognitionService = (binder as RecognitionService.MyBinder).getService()
            recognitionServiceIsBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recognitionService = null
            recognitionServiceIsBounded = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager?.unmute(NOTIFICATION_SERVICE)
        unbindService(recognitionServiceConnection)
    }
}


//class MainActivity : ComponentActivity() {
//        override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            var value by remember {
//                mutableStateOf("------------")
//            }
//            Column() {
//                Text(text = value, color = Red, fontSize = 26.sp)
//                Button(onClick = {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        value = ChatData.getResponse("Привет, какие цвета ты знаешь? Назови 10 штук.")
//                    } }) {
//                    Text("getResponse")
//                }
//            }
//
//        }
//    }
//}







