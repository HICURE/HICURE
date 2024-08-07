package com.example.hicure

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlarmList : AppCompatActivity() {

    private val REQUEST_CODE_SET_ALARM = 1
    private var lastClickedAlarmBox: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)

        val alarmBoxBlue = findViewById<CardView>(R.id.alarmBoxBlue)
        val alarmBoxYellow = findViewById<CardView>(R.id.alarmBoxYellow)
        val alarmBoxPink = findViewById<CardView>(R.id.alarmBoxPink)

        alarmBoxBlue.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxBlue
            navigateToSetAlarm(R.drawable.set_alarm_box_blue, R.drawable.alarm_switch_track_on_blue, R.drawable.set_alarm_save_button_box_blue)
        }

        alarmBoxYellow.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxYellow
            navigateToSetAlarm(R.drawable.set_alarm_box_yellow, R.drawable.alarm_switch_track_on_yellow, R.drawable.set_alarm_save_button_box_yellow)
        }

        alarmBoxPink.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxPink
            navigateToSetAlarm(R.drawable.set_alarm_box_pink, R.drawable.alarm_switch_track_on_pink, R.drawable.set_alarm_save_button_box_pink)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun navigateToSetAlarm(boxDrawableResId: Int, switchDrawableResId: Int, buttonDrawableResId: Int) {
        val intent = Intent(this, SetAlarm::class.java)
        intent.putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
        intent.putExtra("EXTRA_SWITCH_COLOR", switchDrawableResId)
        intent.putExtra("EXTRA_BUTTON_COLOR", buttonDrawableResId)
        startActivityForResult(intent, REQUEST_CODE_SET_ALARM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SET_ALARM && resultCode == Activity.RESULT_OK) {
            data?.let {
                val selectedTime = it.getStringExtra("EXTRA_SELECTED_TIME")
                val alarmName = it.getStringExtra("EXTRA_ALARM_NAME")
                val isSoundVibrationOn = it.getBooleanExtra("EXTRA_SOUND_VIBRATION", false)

                // Update the UI with the new alarm information
                updateAlarmBox(selectedTime, alarmName, isSoundVibrationOn)
            }
        }
    }

    private fun updateAlarmBox(time: String?, name: String?, isSoundVibrationOn: Boolean) {
        val alarmBox = findViewById<CardView>(lastClickedAlarmBox)
        val timeTextViewId = when (lastClickedAlarmBox) {
            R.id.alarmBoxBlue -> R.id.alarmTimeBlue
            R.id.alarmBoxYellow -> R.id.alarmTimeYellow
            R.id.alarmBoxPink -> R.id.alarmTimePink
            else -> R.id.alarmTimeBlue // Default to blue if something unexpected happens
        }
        val labelTextViewId = when (lastClickedAlarmBox) {
            R.id.alarmBoxBlue -> R.id.alarmLabelBlue
            R.id.alarmBoxYellow -> R.id.alarmLabelYellow
            R.id.alarmBoxPink -> R.id.alarmLabelPink
            else -> R.id.alarmLabelBlue // Default to blue if something unexpected happens
        }

        alarmBox.findViewById<TextView>(timeTextViewId).text = time
        alarmBox.findViewById<TextView>(labelTextViewId).text = name

        // You can update the sound/vibration status here if needed
        // For example:
        // val soundVibrationIcon = alarmBox.findViewById<ImageView>(R.id.soundVibrationIcon)
        // soundVibrationIcon.visibility = if (isSoundVibrationOn) View.VISIBLE else View.GONE
    }
}