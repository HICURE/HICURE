package com.example.hicure.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.hicure.R

class NotificationHelper(base: Context) : ContextWrapper(base) {

    private val channelID = "channelID" // 알림 채널 ID
    private val channelNm = "채널 이름" // 알림 채널 이름

    init {
        // 안드로이드 버전이 오레오(Oreo) 이상인 경우 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    // 알림 채널 생성
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(channelID, channelNm, NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        // NotificationManager를 통해 채널 등록
        getManager().createNotificationChannel(channel)
    }

    // NotificationManager 객체 반환
    fun getManager(): NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // 알림 설정
    fun getChannelNotification(time: String?): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle(time)
            .setContentText("폐활량 측정 시간입니다.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}