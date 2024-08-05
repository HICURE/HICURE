package com.example.hicure

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlarmList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)

        val alarmBoxBlue = findViewById<CardView>(R.id.alarmBoxBlue)
        val alarmBoxYellow = findViewById<CardView>(R.id.alarmBoxYellow)
        val alarmBoxPink = findViewById<CardView>(R.id.alarmBoxPink)

        alarmBoxBlue.setOnClickListener {
            navigateToSetAlarm(R.drawable.set_alarm_box_blue, R.drawable.alarm_switch_track_on_blue, R.drawable.set_alarm_save_button_box_blue)
        }

        alarmBoxYellow.setOnClickListener {
            navigateToSetAlarm(R.drawable.set_alarm_box_yellow, R.drawable.alarm_switch_track_on_yellow, R.drawable.set_alarm_save_button_box_yellow)
        }

        alarmBoxPink.setOnClickListener {
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
        startActivity(intent)
    }
}
