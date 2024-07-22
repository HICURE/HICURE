package com.example.hicure

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class AlarmList : AppCompatActivity() {
    private lateinit var alarmBoxLayout: ConstraintLayout
    private lateinit var alarmSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)

        alarmBoxLayout = findViewById(R.id.alarmBoxLayout)
        alarmSwitch = findViewById(R.id.alarmSwitch)

        // 초기 스타일 설정
        setAlarmBoxStyle("grey")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setAlarmBoxStyle(style: String) {
        when (style) {
            "blue" -> {
                alarmBoxLayout.setBackgroundResource(R.drawable.alarm_box_blue)
                alarmBoxLayout.setTag(R.attr.alarmBoxStyle, 0)
                alarmSwitch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.switch_track_color_on_blue))
            }
            "yellow" -> {
                alarmBoxLayout.setBackgroundResource(R.drawable.alarm_box_yellow)
                alarmBoxLayout.setTag(R.attr.alarmBoxStyle, 1)
                alarmSwitch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.switch_track_color_on_yellow))
            }
            "pink" -> {
                alarmBoxLayout.setBackgroundResource(R.drawable.alarm_box_pink)
                alarmBoxLayout.setTag(R.attr.alarmBoxStyle, 2)
                alarmSwitch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.switch_track_color_on_pink))
            }
        }
    }
}