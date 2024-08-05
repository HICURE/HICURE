package com.example.hicure

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

        val timePicker = findViewById<CustomTimePickerdd>(R.id.customTimePicker)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val soundVibrationSwitch = findViewById<Switch>(R.id.soundVibrationSwitch)

        saveButton.setOnClickListener {
            val selectedTime = timePicker.getSelectedTime()
            Toast.makeText(this, "Selected Time: $selectedTime", Toast.LENGTH_SHORT).show()
        }

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
//        soundVibrationSwitch.thumbDrawable = switchDrawable

        saveButton.background = buttonDrawable
    }
}
