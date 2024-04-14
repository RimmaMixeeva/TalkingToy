package mr.talkingtoy.engine

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log


class MP3Service: Service() {

    var mPlayer: MediaPlayer? = null
    private val binder = MyBinder()

    override fun onBind(p0: Intent?): IBinder? {
      return binder
    }

    fun initPlayer(mp3: Int) {
        Log.d("Media Player","MediaPlayer init" )
        if(mPlayer == null) {
        mPlayer = MediaPlayer.create(this, mp3)
        }
    }

    fun startPlay(){
        Log.d("Media Player","MediaPlayer start" )
        mPlayer?.start()
    }
    fun stopPlay(){
        Log.d("Media Player","MediaPlayer stop" )
        mPlayer?.release()
        mPlayer = null
    }
    fun pausePlay(){
        Log.d("Media Player","MediaPlayer pause" )
        mPlayer?.pause()
    }
    inner class MyBinder : Binder() {
        fun getService(): MP3Service {
            return this@MP3Service
        }
    }
    fun isOn(): Boolean {
        return (mPlayer!=null)
    }

}