package mr.talkingtoy.engine

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SoundManager (private val context: Activity) {
    private val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

    var priorStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

    fun mute(){
        priorStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            0,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
    }

    fun unmute(permission: String){
        Log.d("TEST5", priorStreamVolume.toString())
            audioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                priorStreamVolume,
                AudioManager.FLAG_ALLOW_RINGER_MODES
            )
    }
    fun soundIsOn(): Boolean{
        return audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0
    }
}