package com.example.hicure.alarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hicure.R
import com.example.hicure.databinding.ActivitySetAlarmBinding
import java.util.Calendar

class SetAlarm : AppCompatActivity() {

    private lateinit var binding: ActivitySetAlarmBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: CustomTimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = binding.customTimePicker

        val initialTime = intent.getStringExtra("EXTRA_ALARM_TIME")
        val amPm = intent.getStringExtra("EXTRA_AM_PM")
        val boxDrawableResId = intent.getIntExtra("EXTRA_BOX_COLOR", R.drawable.set_alarm_box_blue)
        val switchDrawableResId =
            intent.getIntExtra("EXTRA_SWITCH_COLOR", R.drawable.alarm_switch_track_on_blue)
        val buttonDrawableResId =
            intent.getIntExtra("EXTRA_BUTTON_COLOR", R.drawable.set_alarm_save_button_box_blue)
        val alarmName = intent.getStringExtra("EXTRA_ALARM_NAME")
        val isAlarmEnabled = intent.getBooleanExtra("EXTRA_IS_ALARM_ENABLED", false)

        val boxDrawable = ContextCompat.getDrawable(this, boxDrawableResId)
        val switchDrawable = ContextCompat.getDrawable(this, switchDrawableResId)
        val buttonDrawable = ContextCompat.getDrawable(this, buttonDrawableResId)

        binding.alarmBox.background = boxDrawable
        binding.boxLayout.background = boxDrawable
        binding.soundVibrationSwitch.trackDrawable = switchDrawable
        binding.saveButton.background = buttonDrawable

        if (initialTime != null) {
            timePicker.setSelectedTime(initialTime)
        }

        if (isAlarmEnabled) {
            binding.soundVibrationSwitch.isChecked = true
        }

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            val alarmName = binding.alarmNameEditText.text.toString()
            val isAlarmEnabled = binding.soundVibrationSwitch.isChecked

            saveAlarmSettings(selectedTime)
            if (isAlarmEnabled) {
                scheduleAlarm(selectedTime)
            } else {
                cancelAlarm()
            }

            val resultIntent = Intent().apply {
                putExtra("EXTRA_SELECTED_TIME", selectedTime)
                putExtra("EXTRA_ALARM_NAME", alarmName)
                putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
                putExtra("EXTRA_AM_PM", amPm)
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
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, AlertReceiver::class.java).apply {
            putExtra("time", time)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun cancelAlarm() {
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}
