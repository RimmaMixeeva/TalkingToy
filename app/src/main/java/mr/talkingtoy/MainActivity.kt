package mr.talkingtoy

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mr.talkingtoy.ai.ChatUiEvent
import mr.talkingtoy.ai.ChatViewModel
import mr.talkingtoy.engine.RecognitionService
import mr.talkingtoy.engine.ResInfo
import mr.talkingtoy.engine.SettingsManager
import mr.talkingtoy.engine.SoundManager
import mr.talkingtoy.engine.Spinner


class MainActivity : ComponentActivity() {

    private var recognitionService: RecognitionService? = null
    private var recognitionServiceIsBounded: Boolean = false
    private var soundManager: SoundManager? = null
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        context = this.applicationContext
        super.onCreate(savedInstanceState)
        val intent = Intent(this, RecognitionService::class.java)
        bindService(intent, recognitionServiceConnection, BIND_AUTO_CREATE)
        soundManager = SoundManager(this)
        soundManager?.mute()

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
            recognitionServiceIsBounded = true;
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
//
//class MainActivity : ComponentActivity() {
//
//    private val uriState = MutableStateFlow("")
//
//    private val imagePicker =
//        registerForActivityResult<PickVisualMediaRequest, Uri>(
//            ActivityResultContracts.PickVisualMedia()
//        ) { uri ->
//            uri?.let {
//                uriState.update { uri.toString() }
//            }
//        }
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//
//                    Scaffold(
//                        topBar = {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .background(MaterialTheme.colorScheme.primary)
//                                    .height(35.dp)
//                                    .padding(horizontal = 16.dp)
//                            ) {
//                                Text(
//                                    modifier = Modifier
//                                        .align(Alignment.TopStart),
//                                    text = stringResource(id = R.string.app_name),
//                                    fontSize = 19.sp,
//                                    color = MaterialTheme.colorScheme.onPrimary
//                                )
//                            }
//                        }
//                    ) {
//                        ChatScreen(paddingValues = it)
//                    }
//
//                }
//        }
//    }
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun ChatScreen(paddingValues: PaddingValues) {
//        val chaViewModel = viewModel<ChatViewModel>()
//        val chatState = chaViewModel.chatState.collectAsState().value
//
//        val bitmap = getBitmap()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = paddingValues.calculateTopPadding()),
//            verticalArrangement = Arrangement.Bottom
//        ) {
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp),
//                reverseLayout = true
//            ) {
//                itemsIndexed(chatState.chatList) { index, chat ->
//                    if (chat.isFromUser) {
//                        UserChatItem(
//                            prompt = chat.prompt, bitmap = chat.bitmap
//                        )
//                    } else {
//                        ModelChatItem(response = chat.prompt)
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                Column {
//                    bitmap?.let {
//                        Image(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .padding(bottom = 2.dp)
//                                .clip(RoundedCornerShape(6.dp)),
//                            contentDescription = "picked image",
//                            contentScale = ContentScale.Crop,
//                            bitmap = it.asImageBitmap()
//                        )
//                    }
//
//                    Icon(
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clickable {
//                                imagePicker.launch(
//                                    PickVisualMediaRequest
//                                        .Builder()
//                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                                        .build()
//                                )
//                            },
//                        imageVector = Icons.Rounded.AddPhotoAlternate,
//                        contentDescription = "Add Photo",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                TextField(
//                    modifier = Modifier
//                        .weight(1f),
//                    value = chatState.prompt,
//                    onValueChange = {
//                        chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
//                    },
//                    placeholder = {
//                        Text(text = "Type a prompt")
//                    }
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                Icon(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clickable {
//                            chaViewModel.onEvent(ChatUiEvent.SendPrompt(chatState.prompt, bitmap))
//                            uriState.update { "" }
//                        },
//                    imageVector = Icons.Rounded.Send,
//                    contentDescription = "Send prompt",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//
//            }
//
//        }
//
//    }
//
//    @Composable
//    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
//        Column(
//            modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)
//        ) {
//
//            bitmap?.let {
//                Image(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(260.dp)
//                        .padding(bottom = 2.dp)
//                        .clip(RoundedCornerShape(12.dp)),
//                    contentDescription = "image",
//                    contentScale = ContentScale.Crop,
//                    bitmap = it.asImageBitmap()
//                )
//            }
//
//            Text(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.primary)
//                    .padding(16.dp),
//                text = prompt,
//                fontSize = 17.sp,
//                color = MaterialTheme.colorScheme.onPrimary
//            )
//
//        }
//    }
//
//    @Composable
//    fun ModelChatItem(response: String) {
//        Column(
//            modifier = Modifier.padding(end = 100.dp, bottom = 16.dp)
//        ) {
//            Text(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Green)
//                    .padding(16.dp),
//                text = response,
//                fontSize = 17.sp,
//                color = MaterialTheme.colorScheme.onPrimary
//            )
//
//        }
//    }
//
//    @Composable
//    private fun getBitmap(): Bitmap? {
//        val uri = uriState.collectAsState().value
//
//        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(uri)
//                .size(Size.ORIGINAL)
//                .build()
//        ).state
//
//        if (imageState is AsyncImagePainter.State.Success) {
//            return imageState.result.drawable.toBitmap()
//        }
//
//        return null
//    }
//}
//








