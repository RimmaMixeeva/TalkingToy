package mr.talkingtoy

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import mr.talkingtoy.engine.RecognitionService



class MainActivity : ComponentActivity() {
    private var recognitionService: RecognitionService? = null
    private var recognitionServiceIsBounded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, RecognitionService::class.java)
        bindService(intent, recognitionServiceConnection, BIND_AUTO_CREATE)
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
        unbindService(recognitionServiceConnection)
    }

}


