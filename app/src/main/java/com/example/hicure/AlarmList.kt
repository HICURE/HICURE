package com.example.hicure

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.hicure.databinding.ActivityAlarmListBinding


class AlarmList : AppCompatActivity() {
//    private lateinit var alarmBoxLayoutBlue: ConstraintLayout
//    private lateinit var alarmBoxLayoutYellow: ConstraintLayout
//    private lateinit var alarmBoxLayoutPink: ConstraintLayout
//    private lateinit var alarmSwitchBlue: Switch
//    private lateinit var alarmSwitchYellow: Switch
//    private lateinit var alarmSwitchPink: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)

//        alarmBoxLayoutBlue = findViewById(R.id.alarmBoxLayoutBlue)
//        alarmBoxLayoutYellow = findViewById(R.id.alarmBoxLayoutYellow)
//        alarmBoxLayoutPink = findViewById(R.id.alarmBoxLayoutPink)
//        alarmSwitchBlue = findViewById(R.id.alarmSwitchBlue)
//        alarmSwitchYellow = findViewById(R.id.alarmSwitchYellow)
//        alarmSwitchPink = findViewById(R.id.alarmSwitchPink)

//        // 초기 스타일 설정
//        setAlarmBoxStyle("grey")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


//    private fun setAlarmBoxStyle(style: String) {
//        when (style) {
//            "blue" -> {
//                alarmBoxLayoutBlue.setBackgroundResource(R.drawable.alarm_box_blue)
//                alarmBoxLayoutBlue.setTag(R.attr.alarmBoxStyle, 0)
//                alarmSwitchBlue.trackDrawable =ContextCompat.getDrawable(this, R.drawable.alarm_switch_track_on_blue)
//            }
//            "yellow" -> {
//                alarmBoxLayoutYellow.setBackgroundResource(R.drawable.alarm_box_yellow)
//                alarmBoxLayoutYellow.setTag(R.attr.alarmBoxStyle, 1)
//                alarmSwitchYellow.trackDrawable =ContextCompat.getDrawable(this, R.drawable.alarm_switch_track_on_yellow)
//            }
//            "pink" -> {
//                alarmBoxLayoutPink.setBackgroundResource(R.drawable.alarm_box_pink)
//                alarmBoxLayoutPink.setTag(R.attr.alarmBoxStyle, 2)
//                alarmSwitchPink.trackDrawable =ContextCompat.getDrawable(this, R.drawable.alarm_switch_track_on_pink)
//            }
//        }
//    }
}