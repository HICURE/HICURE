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

        // 저장하기 버튼
        saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            val alarmName = alarmNameEditText.text.toString()

            saveAlarmSettings(selectedTime)

            val resultIntent = Intent().apply {
                putExtra("EXTRA_SELECTED_TIME", selectedTime)
                putExtra("EXTRA_ALARM_NAME", alarmName)
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
}