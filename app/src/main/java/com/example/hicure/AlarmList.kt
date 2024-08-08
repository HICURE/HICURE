package com.example.hicure

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.hicure.databinding.ActivityAlarmListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlarmList : AppCompatActivity() {

    private val requestSetAlarm: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val selectedTime = it.getStringExtra("EXTRA_SELECTED_TIME")
                val alarmName = it.getStringExtra("EXTRA_ALARM_NAME")
                val isSoundVibrationOn = it.getBooleanExtra("EXTRA_SOUND_VIBRATION", false)
                updateAlarmBox(selectedTime, alarmName, isSoundVibrationOn)
            }
        }
    }

    private val binding: ActivityAlarmListBinding by lazy {
        ActivityAlarmListBinding.inflate(
            layoutInflater
        )
    }
    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bn_main)
    }

    private var lastClickedAlarmBox: Int = 0

    private val PERMISSION_REQUEST_CODE = 1001

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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SET_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없으므로 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SET_ALARM),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 이미 부여됨
            setupAlarmBoxListeners()
        }

    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SET_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SET_ALARM),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 이미 허용됨
            setupAlarmBoxListeners()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
                setupAlarmBoxListeners()
            } else {
                // 권한이 거부됨, 사용자에게 권한이 필요함을 안내
            }
        }
    }

    private fun setupAlarmBoxListeners() {
        findViewById<CardView>(R.id.alarmBoxLayoutBlue).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxLayoutBlue
            navigateToSetAlarm(
                R.drawable.set_alarm_box_blue,
                R.drawable.alarm_switch_track_on_blue,
                R.drawable.set_alarm_save_button_box_blue
            )
        }
        bottomNagivationView.selectedItemId = R.id.ic_Alarm

        findViewById<CardView>(R.id.alarmBoxLayoutYellow).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxLayoutYellow
            navigateToSetAlarm(
                R.drawable.set_alarm_box_yellow,
                R.drawable.alarm_switch_track_on_yellow,
                R.drawable.set_alarm_save_button_box_yellow
            )
        }

        findViewById<CardView>(R.id.alarmBoxLayoutPink).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxLayoutPink
            navigateToSetAlarm(
                R.drawable.set_alarm_box_pink,
                R.drawable.alarm_switch_track_on_pink,
                R.drawable.set_alarm_save_button_box_pink
            )
        }
    }

    private fun navigateToSetAlarm(
        boxDrawableResId: Int,
        switchDrawableResId: Int,
        buttonDrawableResId: Int
    ) {
        val currentTime = getCurrentTimeForBox(lastClickedAlarmBox)
        val intent = Intent(this, SetAlarm::class.java).apply {
            putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
            putExtra("EXTRA_SWITCH_COLOR", switchDrawableResId)
            putExtra("EXTRA_BUTTON_COLOR", buttonDrawableResId)
            putExtra("EXTRA_ALARM_TIME", currentTime)
        }
        requestSetAlarm.launch(intent)
    }

    private fun getCurrentTimeForBox(alarmBoxId: Int): String {
        return when (alarmBoxId) {
            R.id.alarmBoxLayoutBlue -> findViewById<TextView>(R.id.alarmTimeBlue).text.toString()
            R.id.alarmBoxLayoutYellow -> findViewById<TextView>(R.id.alarmTimeYellow).text.toString()
            R.id.alarmBoxLayoutPink -> findViewById<TextView>(R.id.alarmTimePink).text.toString()
            else -> ""
        }
    }

    private fun updateAlarmBox(time: String?, name: String?, isSoundVibrationOn: Boolean) {
        val alarmBox = findViewById<CardView>(lastClickedAlarmBox)
        val (timeTextViewId, labelTextViewId, amPmTextViewId) = getTextViewIdsForBox(
            lastClickedAlarmBox
        )

        val timeTextView = alarmBox.findViewById<TextView>(timeTextViewId)
        val labelTextView = alarmBox.findViewById<TextView>(labelTextViewId)
        val amPmTextView = alarmBox.findViewById<TextView>(amPmTextViewId)

        timeTextView.typeface = ResourcesCompat.getFont(this, R.font.oxygen_bold)
        val (newTime, amPm) = splitTimeAndAmPm(time)
        timeTextView.text = newTime
        amPmTextView.text = amPm
        labelTextView.text = name

        // Update sound/vibration state if needed
    }

    private fun getTextViewIdsForBox(alarmBoxId: Int): Triple<Int, Int, Int> {
        return when (alarmBoxId) {
            R.id.alarmBoxLayoutBlue -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue)
            R.id.alarmBoxLayoutYellow -> Triple(
                R.id.alarmTimeYellow,
                R.id.alarmLabelYellow,
                R.id.alarmAmPmYellow
            )

            R.id.alarmBoxLayoutPink -> Triple(R.id.alarmTimePink, R.id.alarmLabelPink, R.id.alarmAmPmPink)
            else -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue) // Default
        }
    }

    private fun splitTimeAndAmPm(time: String?): Pair<String, String> {
        return time?.split(" ")?.let { parts ->
            if (parts.size == 2) Pair(parts[0], parts[1]) else Pair(time, "")
        } ?: Pair("", "")
    }
}