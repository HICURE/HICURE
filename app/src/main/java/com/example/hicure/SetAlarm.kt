package com.example.hicure

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.util.Calendar

class SetAlarm : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: CustomTimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = findViewById(R.id.customTimePicker)

        val alarmNameEditText = findViewById<EditText>(R.id.alarmNameEditText)
        val soundVibrationSwitch = findViewById<Switch>(R.id.soundVibrationSwitch)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        val initialTime = intent.getStringExtra("EXTRA_ALARM_TIME")
        val boxDrawableResId = intent.getIntExtra("EXTRA_BOX_COLOR", R.drawable.set_alarm_box_blue)
        val switchDrawableResId = intent.getIntExtra("EXTRA_SWITCH_COLOR", R.drawable.alarm_switch_track_on_blue)
        val buttonDrawableResId = intent.getIntExtra("EXTRA_BUTTON_COLOR", R.drawable.set_alarm_save_button_box_blue)

        val boxDrawable = ContextCompat.getDrawable(this, boxDrawableResId)
        val switchDrawable = ContextCompat.getDrawable(this, switchDrawableResId)
        val buttonDrawable = ContextCompat.getDrawable(this, buttonDrawableResId)

        findViewById<CardView>(R.id.alarmBox).background = boxDrawable
        findViewById<LinearLayout>(R.id.boxLayout).background = boxDrawable
        soundVibrationSwitch.trackDrawable = switchDrawable
        saveButton.background = buttonDrawable

        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            val alarmName = alarmNameEditText.text.toString()
            val isAlarmEnabled = soundVibrationSwitch.isChecked // 알람 on, off

            saveAlarmSettings(selectedTime)
            scheduleAlarm(selectedTime)  // Add this method to schedule the alarm

            if (isAlarmEnabled) {
                scheduleAlarm(selectedTime) // Schedule alarm with repeat
            } else {
                cancelAlarm()
            }


            val resultIntent = Intent().apply {
                putExtra("EXTRA_SELECTED_TIME", selectedTime)
                putExtra("EXTRA_ALARM_NAME", alarmName)
                putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
                putExtra("EXTRA_IS_ALARM_ENABLED", isAlarmEnabled)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun saveAlarmSettings(time: String) {
        getSharedPreferences("alarm_prefs", MODE_PRIVATE).edit().apply {
            putString("alarm_time", time)
            apply()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(time: String) {
        val calendar = Calendar.getInstance()
        val hourMinute = time.split(" ")[0]
        val hour = hourMinute.split(":")[0].toInt()
        val minute = hourMinute.split(":")[1].toInt()
        val amPm = time.split(" ")[1]

        // Set the calendar time based on 12-hour format
        if (amPm.equals("PM", ignoreCase = true) && hour < 12) {
            calendar.set(Calendar.HOUR_OF_DAY, hour + 12)
        } else if (amPm.equals("AM", ignoreCase = true) && hour == 12) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
        }
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Set for next day
        }

        // Set alarm intent
        val intent = Intent(this, AlertReceiver::class.java).apply {
            putExtra("time", time)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun cancelAlarm() {
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}
