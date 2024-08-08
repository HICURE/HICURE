package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.hicure.databinding.ActivityAlarmListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlarmList : AppCompatActivity() {
    //    private lateinit var alarmBoxLayoutBlue: ConstraintLayout
    //    private lateinit var alarmBoxLayoutYellow: ConstraintLayout
    //    private lateinit var alarmBoxLayoutPink: ConstraintLayout
    //    private lateinit var alarmSwitchBlue: Switch
    //    private lateinit var alarmSwitchYellow: Switch
    //    private lateinit var alarmSwitchPink: Switch
    private val binding: ActivityAlarmListBinding by lazy { ActivityAlarmListBinding.inflate(layoutInflater) }
    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bn_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bottomNagivationView.selectedItemId = R.id.ic_Alarm

        binding.bnMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_Home -> startNewActivity(MainActivity::class.java)
                R.id.ic_Alarm -> startNewActivity(AlarmList::class.java)
                R.id.ic_Serve -> startNewActivity(ServeInfo::class.java)
                R.id.ic_User -> startNewActivity(UserInfo::class.java)
            }
            true
        }
        bottomNagivationView.selectedItemId = R.id.ic_Alarm
    }
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
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
