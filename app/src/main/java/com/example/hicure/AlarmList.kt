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

    private var lastClickedAlarmBox: Int = 0

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으므로 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SET_ALARM), PERMISSION_REQUEST_CODE)
        } else {
            // 권한이 이미 부여됨
            setupAlarmBoxListeners()
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SET_ALARM), PERMISSION_REQUEST_CODE)
        } else {
            // 권한이 이미 허용됨
            setupAlarmBoxListeners()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        findViewById<CardView>(R.id.alarmBoxBlue).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxBlue
            navigateToSetAlarm(R.drawable.set_alarm_box_blue, R.drawable.alarm_switch_track_on_blue, R.drawable.set_alarm_save_button_box_blue)
        }

        findViewById<CardView>(R.id.alarmBoxYellow).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxYellow
            navigateToSetAlarm(R.drawable.set_alarm_box_yellow, R.drawable.alarm_switch_track_on_yellow, R.drawable.set_alarm_save_button_box_yellow)
        }

        findViewById<CardView>(R.id.alarmBoxPink).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxPink
            navigateToSetAlarm(R.drawable.set_alarm_box_pink, R.drawable.alarm_switch_track_on_pink, R.drawable.set_alarm_save_button_box_pink)
        }
    }

    private fun navigateToSetAlarm(boxDrawableResId: Int, switchDrawableResId: Int, buttonDrawableResId: Int) {
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
            R.id.alarmBoxBlue -> findViewById<TextView>(R.id.alarmTimeBlue).text.toString()
            R.id.alarmBoxYellow -> findViewById<TextView>(R.id.alarmTimeYellow).text.toString()
            R.id.alarmBoxPink -> findViewById<TextView>(R.id.alarmTimePink).text.toString()
            else -> ""
        }
    }

    private fun updateAlarmBox(time: String?, name: String?, isSoundVibrationOn: Boolean) {
        val alarmBox = findViewById<CardView>(lastClickedAlarmBox)
        val (timeTextViewId, labelTextViewId, amPmTextViewId) = getTextViewIdsForBox(lastClickedAlarmBox)

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
            R.id.alarmBoxBlue -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue)
            R.id.alarmBoxYellow -> Triple(R.id.alarmTimeYellow, R.id.alarmLabelYellow, R.id.alarmAmPmYellow)
            R.id.alarmBoxPink -> Triple(R.id.alarmTimePink, R.id.alarmLabelPink, R.id.alarmAmPmPink)
            else -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue) // Default
        }
    }

    private fun splitTimeAndAmPm(time: String?): Pair<String, String> {
        return time?.split(" ")?.let { parts ->
            if (parts.size == 2) Pair(parts[0], parts[1]) else Pair(time, "")
        } ?: Pair("", "")
    }
}