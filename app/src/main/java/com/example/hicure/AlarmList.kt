package com.example.hicure

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

class AlarmList : AppCompatActivity() {

    private lateinit var requestSetAlarm: ActivityResultLauncher<Intent>
    private var lastClickedAlarmBox: Int = 0
    private var initialTime: String = ""
    private var AMPMText: String = ""
    private var initialLabelText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)
        setupAlarmBoxListeners()
        initActivityResultLauncher()
    }

    private fun setupAlarmBoxListeners() {
        findViewById<CardView>(R.id.alarmBoxBlue).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxBlue
            initialTime = getCurrentTimeForBox(R.id.alarmBoxBlue)
            AMPMText = getAMPMForBox(R.id.alarmBoxBlue)
            initialLabelText = getLabelTextForBox(R.id.alarmBoxBlue)
            navigateToSetAlarm(R.drawable.set_alarm_box_blue, R.drawable.alarm_switch_track_on_blue, R.drawable.set_alarm_save_button_box_blue)
        }

        findViewById<CardView>(R.id.alarmBoxYellow).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxYellow
            initialTime = getCurrentTimeForBox(R.id.alarmBoxYellow)
            AMPMText = getAMPMForBox(R.id.alarmBoxYellow)
            initialLabelText = getLabelTextForBox(R.id.alarmBoxYellow)
            navigateToSetAlarm(R.drawable.set_alarm_box_yellow, R.drawable.alarm_switch_track_on_yellow, R.drawable.set_alarm_save_button_box_yellow)
        }

        findViewById<CardView>(R.id.alarmBoxPink).setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxPink
            initialTime = getCurrentTimeForBox(R.id.alarmBoxPink)
            AMPMText = getAMPMForBox(R.id.alarmBoxPink)
            initialLabelText = getLabelTextForBox(R.id.alarmBoxPink)
            navigateToSetAlarm(R.drawable.set_alarm_box_pink, R.drawable.alarm_switch_track_on_pink, R.drawable.set_alarm_save_button_box_pink)
        }
    }

    private fun initActivityResultLauncher() {
        requestSetAlarm = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val selectedTime = it.getStringExtra("EXTRA_SELECTED_TIME")
                    val alarmName = it.getStringExtra("EXTRA_ALARM_NAME")
                    val isAlarmEnabled = it.getBooleanExtra("EXTRA_IS_ALARM_ENABLED", false)
                    updateAlarmBox(selectedTime, alarmName, isAlarmEnabled)
                }
            }
        }
    }

    private fun navigateToSetAlarm(boxDrawableResId: Int, switchDrawableResId: Int, buttonDrawableResId: Int) {
        val intent = Intent(this, SetAlarm::class.java).apply {
            putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
            putExtra("EXTRA_SWITCH_COLOR", switchDrawableResId)
            putExtra("EXTRA_BUTTON_COLOR", buttonDrawableResId)
            putExtra("EXTRA_ALARM_TIME", initialTime)
            putExtra("EXTRA_AM_PM", AMPMText)
            putExtra("EXTRA_LABEL_TEXT", initialLabelText)
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

    private fun getAMPMForBox(alarmBoxId: Int): String {
        return when (alarmBoxId) {
            R.id.alarmBoxBlue -> findViewById<TextView>(R.id.alarmAmPmBlue).text.toString()
            R.id.alarmBoxYellow -> findViewById<TextView>(R.id.alarmAmPmYellow).text.toString()
            R.id.alarmBoxPink -> findViewById<TextView>(R.id.alarmAmPmPink).text.toString()
            else -> ""
        }
    }

    private fun getLabelTextForBox(alarmBoxId: Int): String {
        return when (alarmBoxId) {
            R.id.alarmBoxBlue -> findViewById<TextView>(R.id.alarmLabelBlue).text.toString()
            R.id.alarmBoxYellow -> findViewById<TextView>(R.id.alarmLabelYellow).text.toString()
            R.id.alarmBoxPink -> findViewById<TextView>(R.id.alarmLabelPink).text.toString()
            else -> ""
        }
    }

    private fun updateAlarmBox(time: String?, name: String?, isAlarmEnabled: Boolean) {
        val alarmBox = findViewById<CardView>(lastClickedAlarmBox)
        val textViewIds = getTextViewIdsForBox(lastClickedAlarmBox)

        val timeTextView = alarmBox.findViewById<TextView>(textViewIds.first)
        val labelTextView = alarmBox.findViewById<TextView>(textViewIds.second)
        val amPmTextView = alarmBox.findViewById<TextView>(textViewIds.third)

        timeTextView.typeface = ResourcesCompat.getFont(this, R.font.oxygen_bold)
        val (newTime, amPm) = splitTimeAndAmPm(time)
        timeTextView.text = newTime
        amPmTextView.text = amPm
        labelTextView.text = name
    }

    private fun getTextViewIdsForBox(alarmBoxId: Int): Triple<Int, Int, Int> {
        return when (alarmBoxId) {
            R.id.alarmBoxBlue -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue)
            R.id.alarmBoxYellow -> Triple(R.id.alarmTimeYellow, R.id.alarmLabelYellow, R.id.alarmAmPmYellow)
            R.id.alarmBoxPink -> Triple(R.id.alarmTimePink, R.id.alarmLabelPink, R.id.alarmAmPmPink)
            else -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue)
        }
    }

    private fun splitTimeAndAmPm(time: String?): Pair<String, String> {
        return time?.split(" ")?.let { parts ->
            if (parts.size == 2) Pair(parts[0], parts[1]) else Pair(time, "")
        } ?: Pair("", "")
    }
}
