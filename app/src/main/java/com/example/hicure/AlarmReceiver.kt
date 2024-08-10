package com.example.hicure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationHelper = NotificationHelper(context!!)

        // 넘어온 데이터
        val time = intent?.extras?.getString("time")

        val nb: NotificationCompat.Builder = notificationHelper.getChannelNotification(time)

        // 알림 호출
        notificationHelper.getManager().notify(1, nb.build())
    }
}
