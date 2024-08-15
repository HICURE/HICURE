package com.example.hicure.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            val notificationHelper = NotificationHelper(ctx)
            val database = AlarmDatabase.getInstance(ctx)
            val alarmRepository = AlarmRepository(database.alarmDao())
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            CoroutineScope(Dispatchers.IO).launch {
                val alarmId = intent?.getIntExtra("EXTRA_ALARM_ID", -1) ?: -1
                if (alarmId != -1) {
                    val alarmEntity = alarmRepository.getAlarmById(alarmId)
                    withContext(Dispatchers.Main) {
                        alarmEntity?.let { alarm ->
                            val nb: NotificationCompat.Builder = notificationHelper.getChannelNotification(alarm.time)
                            notificationHelper.getManager().notify(alarmId, nb.build())

                            // 다음 날 같은 시간으로 알람 재설정
                            rescheduleAlarm(ctx, alarm, alarmManager)
                        }
                    }
                }
            }
        }
    }
    @SuppressLint("ObsoleteSdkInt", "ScheduleExactAlarm")
    private fun rescheduleAlarm(context: Context, alarm: AlarmEntity, alarmManager: AlarmManager) {
        val calendar = Calendar.getInstance().apply {
            val hourMinute = alarm.time.split(" ")[0]
            val hour = hourMinute.split(":")[0].toInt()
            val minute = hourMinute.split(":")[1].toInt()
            val amPm = alarm.amPm

            if (amPm.equals("PM", ignoreCase = true) && hour < 12) {
                set(Calendar.HOUR_OF_DAY, hour + 12)
            } else if (amPm.equals("AM", ignoreCase = true) && hour == 12) {
                set(Calendar.HOUR_OF_DAY, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // 다음 날로 설정
        }

        val intent = Intent(context, AlertReceiver::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
