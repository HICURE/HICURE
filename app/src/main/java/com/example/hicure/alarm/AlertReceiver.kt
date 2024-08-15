package com.example.hicure.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            val notificationHelper = NotificationHelper(ctx)
            val database = AlarmDatabase.getInstance(ctx)
            val alarmRepository = AlarmRepository(database.alarmDao())

            CoroutineScope(Dispatchers.IO).launch {
                val alarmId = intent?.getIntExtra("EXTRA_ALARM_ID", -1) ?: -1
                if (alarmId != -1) {
                    val alarmEntity = alarmRepository.getAlarmById(alarmId)
                    withContext(Dispatchers.Main) {
                        alarmEntity?.let { alarm ->
                            val nb: NotificationCompat.Builder = notificationHelper.getChannelNotification(alarm.time)
                            notificationHelper.getManager().notify(alarmId, nb.build())
                        }
                    }
                }
            }
        }
    }
}
