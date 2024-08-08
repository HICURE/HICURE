package com.example.hicure

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.util.*
import java.util.Calendar
import java.text.SimpleDateFormat
import androidx.activity.result.contract.ActivityResultContracts

class SetAlarm : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: CustomTimePicker

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            setAlarm(timePicker.getSelectedTime())
        } else {
            showAlarmPermissionDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = findViewById(R.id.customTimePicker)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val soundVibrationSwitch = findViewById<Switch>(R.id.soundVibrationSwitch)
        val alarmNameEditText = findViewById<EditText>(R.id.alarmNameEditText)

        val boxDrawableResId = intent.getIntExtra("EXTRA_BOX_COLOR", R.drawable.set_alarm_box_blue)
        val switchDrawableResId = intent.getIntExtra("EXTRA_SWITCH_COLOR", R.drawable.alarm_switch_track_on_blue)
        val buttonDrawableResId = intent.getIntExtra("EXTRA_BUTTON_COLOR", R.drawable.set_alarm_save_button_box_blue)
        val initialTime = intent.getStringExtra("EXTRA_ALARM_TIME")

        val boxDrawable = ContextCompat.getDrawable(this, boxDrawableResId)
        val switchDrawable = ContextCompat.getDrawable(this, switchDrawableResId)
        val buttonDrawable = ContextCompat.getDrawable(this, buttonDrawableResId)

        findViewById<CardView>(R.id.alarmBox).background = boxDrawable
        findViewById<LinearLayout>(R.id.boxLayout).background = boxDrawable
        soundVibrationSwitch.trackDrawable = switchDrawable
        saveButton.background = buttonDrawable

         initialTime?.let { timePicker.setSelectedTime(it) }

        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            val alarmName = alarmNameEditText.text.toString()
            val isSoundVibrationOn = soundVibrationSwitch.isChecked

            saveAlarmSettings(selectedTime)
            checkAlarmPermission()

            val resultIntent = Intent().apply {
                putExtra("EXTRA_SELECTED_TIME", selectedTime)
                putExtra("EXTRA_ALARM_NAME", alarmName)
                putExtra("EXTRA_SOUND_VIBRATION", isSoundVibrationOn)
                putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
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

    private fun setAlarm(time: String) {
        try {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val calendar = Calendar.getInstance().apply {
                val timeParts = time.split(":")
                val (hour, minute) = timeParts[0].trim().split(" ")[0].toInt() to timeParts[1].split(" ")[0].toInt()
                val amPm = timeParts[1].split(" ")[1]

                set(Calendar.HOUR_OF_DAY, if (amPm.equals("PM", ignoreCase = true) && hour != 12) hour + 12 else if (amPm.equals("AM", ignoreCase = true) && hour == 12) 0 else hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }

            println("Alarm set for: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)}")
        } catch (e: SecurityException) {
            showAlarmPermissionDialog()
        }
    }

    private fun checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showAlarmPermissionDialog()
            } else {
                setAlarm(timePicker.getSelectedTime())
            }
        } else {
            setAlarm(timePicker.getSelectedTime())
        }
    }

    private fun showAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("알람 권한 필요")
            .setMessage("정확한 알람 설정을 위해 권한이 필요합니다. 설정으로 이동하시겠습니까?")
            .setPositiveButton("설정으로 이동") { _, _ -> requestAlarmPermission() }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                it.data = Uri.parse("package:$packageName")
                requestPermissionLauncher.launch(it)
            }
        }
    }
}