package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class SetAlarm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)

        val timePicker = findViewById<CustomTimePickerdd>(R.id.customTimePicker) // 시간 설정
        val cancelButton = findViewById<Button>(R.id.cancelButton) // 취소 버튼
        val saveButton = findViewById<Button>(R.id.saveButton) // 저장 버튼
        val soundVibrationSwitch = findViewById<Switch>(R.id.soundVibrationSwitch) // 소리 및 진동 토글 버튼

        val boxDrawableResId = intent.getIntExtra("EXTRA_BOX_COLOR", R.drawable.set_alarm_box_blue)
        val switchDrawableResId = intent.getIntExtra("EXTRA_SWITCH_COLOR", R.drawable.alarm_switch_track_on_blue)
        val buttonDrawableResId = intent.getIntExtra("EXTRA_BUTTON_COLOR", R.drawable.set_alarm_save_button_box_blue)

        val boxDrawable = ContextCompat.getDrawable(this, boxDrawableResId)
        val switchDrawable = ContextCompat.getDrawable(this, switchDrawableResId)
        val buttonDrawable = ContextCompat.getDrawable(this, buttonDrawableResId)

        val cardView = findViewById<CardView>(R.id.alarmBox)
        cardView.background = boxDrawable

        val boxLayout = findViewById<LinearLayout>(R.id.boxLayout)
        boxLayout.background = boxDrawable

        soundVibrationSwitch.trackDrawable = switchDrawable
        saveButton.background = buttonDrawable

        // 취소 버튼
        cancelButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            Toast.makeText(this, "Selected Time: $selectedTime", Toast.LENGTH_SHORT).show()
        }

        // 저장 버튼
        saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            val resultIntent = Intent()
            resultIntent.putExtra("EXTRA_SELECTED_TIME", selectedTime)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
