package com.example.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast


class MusicPlayerService : Service() {

    var mMediaPlayer: MediaPlayer? = null // 미디어 플레이어 객체를 null로 초기화

    var mBinder: MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onBind(intent: Intent?): IBinder? {   // ❷ 바인더 반환
        return mBinder
    }

    // ❸ startService()를 호출하면 실행되는 콜백 함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }



    // ❶ 알림 채널 생성
    fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel( // 알림 채널을 생성합니다.
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        // ❷ 알림 생성
        val notification: Notification = Notification.Builder(this, "CHANNEL_ID")                .setSmallIcon(R.drawable.ic_play) // 알림 아이콘입니다.
            .setContentTitle("뮤직 플레이어 앱")   // 알림의 제목을 설정합니다.
            .setContentText("앱이 실행 중입니다.")  // 알림의 내용을 설정합니다.
            .build()

        startForeground(1, notification)
    }

    // ❸ 서비스 중단 처리
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    // 재생되고 있는지 확인합니다.
    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }


    fun play() { // ❶
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate) // 음악 파일의 리소스를 가져와 미디어 플레이어 객체를 할당해줍니다.
            mMediaPlayer?.setVolume(1.0f, 1.0f);    // 볼륨을 지정해줍니다.
            mMediaPlayer?.isLooping = true      // 반복재생 여부를 정해줍니다.
            mMediaPlayer?.start()                // 음악을 재생합니다.
        } else {
            if (mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악이 실행 중입니다.", Toast.LENGTH_SHORT).show()
            } else {
                mMediaPlayer?.start()        // 음악을 재생합니다.
            }
        }
    }
    fun pause() { // ❷
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause() // 음악을 일시정지합니다.
            }
        }
    }
    fun stop() { // ❸
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()   // 음악을 멈춥니다.
                it.release()    // 미디어 플레이어에 할당된 자원을 해제시켜줍니다.
                mMediaPlayer = null
            }
        }

    }

}
