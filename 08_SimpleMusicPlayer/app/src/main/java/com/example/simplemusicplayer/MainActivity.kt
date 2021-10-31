package com.example.simplemusicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var btn_play: Button
    lateinit var btn_pause: Button
    lateinit var btn_stop: Button
    var mService: MusicPlayerService? = null  // ❶

    val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = (service as MusicPlayerService.MusicPlayerBinder).getService() //MusicPlayerBinder로 타입 캐스팅 해줍니다.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null     // 만약 서비스가 끊기면, mService를 null로 만들어줍니다.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_play = findViewById(R.id.btn_play)
        btn_pause = findViewById(R.id.btn_pause)
        btn_stop = findViewById(R.id.btn_stop)

        btn_play.setOnClickListener(this)
        btn_pause.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_play -> {
                play()
            }

            R.id.btn_pause -> {
                pause()
            }

            R.id.btn_stop -> {
                stop()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mService == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 안드로이드 O 이상인 경우 startForegroundService를 사용해주어야 합니다.
                startForegroundService(Intent(this,
                    MusicPlayerService::class.java))
            } else {
                startService(Intent(applicationContext, MusicPlayerService::class.java))
            }
            val intent = Intent(this, MusicPlayerService::class.java)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)   // 서비스와 바인드합니다.
        }
    }

    override fun onPause() {
        super.onPause()
        if (mService != null) {
            if (!mService!!.isPlaying()) { // 만약 mService가 재생되고 있지 않다면
                mService!!.stopSelf()   // 서비스를 중단해줍니다.
            }
            unbindService(mServiceConnection)   //서비스로부터 연결을 끊습니다.
            mService = null
        }
    }

    private fun play() {
        mService?.play()
    }

    private fun pause() {
        mService?.pause()
    }

    private fun stop() {
        mService?.stop()
    }

}

