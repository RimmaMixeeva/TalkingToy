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
        if(mPlayer == null) {
        mPlayer = MediaPlayer.create(this, mp3)
        }
    }

    fun startPlay(){
        mPlayer?.start()
    }
    fun stopPlay(){
        mPlayer?.release()
        mPlayer = null
    }
    fun pausePlay(){
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