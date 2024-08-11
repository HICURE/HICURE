package com.example.hicure.alarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.example.hicure.R
import com.example.hicure.databinding.ActivityAlarmListBinding

class AlarmList : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmListBinding
    private lateinit var requestSetAlarm: ActivityResultLauncher<Intent>
    private var lastClickedAlarmBox: Int = 0
    private var savedTime: String? = null
    private var savedAmPm: String? = null
    private var savedLabel: String? = null
    private var savedIsEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAlarmBoxListeners()
        initActivityResultLauncher()
    }

    private fun setupAlarmBoxListeners() {
        binding.alarmBoxBlue.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxBlue
            navigateToSetAlarm(
                R.drawable.set_alarm_box_blue,
                R.drawable.alarm_switch_track_on_blue,
                R.drawable.set_alarm_save_button_box_blue
            )
        }

        binding.alarmBoxYellow.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxYellow
            navigateToSetAlarm(
                R.drawable.set_alarm_box_yellow,
                R.drawable.alarm_switch_track_on_yellow,
                R.drawable.set_alarm_save_button_box_yellow
            )
        }

        binding.alarmBoxPink.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxPink
            navigateToSetAlarm(
                R.drawable.set_alarm_box_pink,
                R.drawable.alarm_switch_track_on_pink,
                R.drawable.set_alarm_save_button_box_pink
            )
        }
    }

    private fun initActivityResultLauncher() {
        requestSetAlarm = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    savedTime = it.getStringExtra("EXTRA_SELECTED_TIME")
                    savedAmPm = it.getStringExtra("EXTRA_AM_PM")
                    savedLabel = it.getStringExtra("EXTRA_ALARM_NAME")
                    savedIsEnabled = it.getBooleanExtra("EXTRA_IS_ALARM_ENABLED", false)
                    updateAlarmBox()
                }
            }
        }
    }

    private fun navigateToSetAlarm(boxDrawableResId: Int, switchDrawableResId: Int, buttonDrawableResId: Int) {
        val intent = Intent(this, SetAlarm::class.java).apply {
            putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
            putExtra("EXTRA_SWITCH_COLOR", switchDrawableResId)
            putExtra("EXTRA_BUTTON_COLOR", buttonDrawableResId)
            putExtra("EXTRA_ALARM_TIME", savedTime)
            putExtra("EXTRA_AM_PM", savedAmPm)
            putExtra("EXTRA_ALARM_NAME", savedLabel)
            putExtra("EXTRA_IS_ALARM_ENABLED", savedIsEnabled)
        }
        requestSetAlarm.launch(intent)
    }

    private fun updateAlarmBox() {
        val alarmBox = binding.root.findViewById<CardView>(lastClickedAlarmBox)
        val textViewIds = getTextViewIdsForBox(lastClickedAlarmBox)

        val timeTextView = alarmBox.findViewById<TextView>(textViewIds.first)
        val labelTextView = alarmBox.findViewById<TextView>(textViewIds.second)
        val amPmTextView = alarmBox.findViewById<TextView>(textViewIds.third)

        timeTextView.typeface = ResourcesCompat.getFont(this, R.font.oxygen_bold)
        val (newTime, amPm) = splitTimeAndAmPm(savedTime)
        timeTextView.text = newTime
        amPmTextView.text = amPm
        labelTextView.text = savedLabel
    }

    private fun getTextViewIdsForBox(alarmBoxId: Int): Triple<Int, Int, Int> {
        return when (alarmBoxId) {
            R.id.alarmBoxBlue -> Triple(R.id.alarmTimeBlue, R.id.alarmLabelBlue, R.id.alarmAmPmBlue)
            R.id.alarmBoxYellow -> Triple(
                R.id.alarmTimeYellow,
                R.id.alarmLabelYellow,
                R.id.alarmAmPmYellow
            )
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
