package com.example.hicure.alarm

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmList : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmListBinding
    private lateinit var requestSetAlarm: ActivityResultLauncher<Intent>
    private var lastClickedAlarmBox: Int = 0
    private lateinit var alarmRepository: AlarmRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val database = AlarmDatabase.getInstance(applicationContext)
        alarmRepository = AlarmRepository(database.alarmDao())

        setupAlarmBoxListeners()
        initActivityResultLauncher()
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepository.clearAllAlarms() // Clear the database
            initializeDefaultAlarms() // Initialize default alarms
        }
    }

    private fun initializeDefaultAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if alarms are already set
            val existingAlarms = alarmRepository.getAllAlarms.first()
            if (existingAlarms.isEmpty()) {
                // Insert default alarms
                val defaultAlarms = listOf(
                    AlarmEntity(id = 1, time = "08:00", amPm = "AM", label = "Wake Up", isEnabled = true, isSoundAndVibration = true),
                    AlarmEntity(id = 2, time = "12:00", amPm = "PM", label = "Lunch", isEnabled = true, isSoundAndVibration = true),
                    AlarmEntity(id = 3, time = "06:00", amPm = "PM", label = "Dinner", isEnabled = true, isSoundAndVibration = true)
                )
                defaultAlarms.forEach { alarm ->
                    alarmRepository.insertAlarm(alarm)
                }
            }
        }
    }

    private fun setupAlarmBoxListeners() {
        binding.alarmBoxBlue.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxBlue
            navigateToSetAlarm(
                R.drawable.set_alarm_box_blue,
                R.drawable.alarm_switch_track_on_blue,
                R.drawable.set_alarm_save_button_box_blue,
                1
            )
        }

        binding.alarmBoxYellow.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxYellow
            navigateToSetAlarm(
                R.drawable.set_alarm_box_yellow,
                R.drawable.alarm_switch_track_on_yellow,
                R.drawable.set_alarm_save_button_box_yellow,
                2
            )
        }

        binding.alarmBoxPink.setOnClickListener {
            lastClickedAlarmBox = R.id.alarmBoxPink
            navigateToSetAlarm(
                R.drawable.set_alarm_box_pink,
                R.drawable.alarm_switch_track_on_pink,
                R.drawable.set_alarm_save_button_box_pink,
                3
            )
        }
    }

    private fun initActivityResultLauncher() {
        requestSetAlarm = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    val time = it.getStringExtra("EXTRA_SELECTED_TIME")
                    val amPm = it.getStringExtra("EXTRA_AM_PM")
                    val label = it.getStringExtra("EXTRA_ALARM_NAME")
                    val isEnabled = it.getBooleanExtra("EXTRA_IS_ALARM_ENABLED", false)
                    val id = it.getIntExtra("EXTRA_ALARM_ID", 0)
                    val isSoundAndVibration = it.getBooleanExtra("EXTRA_IS_SOUND_AND_VIBRATION", false)

                    val alarmEntity = AlarmEntity(
                        id = id,
                        time = time ?: "",
                        amPm = amPm ?: "",
                        label = label ?: "",
                        isEnabled = isEnabled,
                        isSoundAndVibration = isSoundAndVibration
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        alarmRepository.insertOrUpdateAlarm(alarmEntity)
                    }

                    updateAlarmBox(time, amPm, label)
                }
            }
        }
    }

    private fun navigateToSetAlarm(boxDrawableResId: Int, switchDrawableResId: Int, buttonDrawableResId: Int, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarmEntity = alarmRepository.getAlarmById(id)
            val intent = Intent(this@AlarmList, SetAlarm::class.java).apply {
                putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
                putExtra("EXTRA_SWITCH_COLOR", switchDrawableResId)
                putExtra("EXTRA_BUTTON_COLOR", buttonDrawableResId)
                putExtra("EXTRA_ALARM_TIME", alarmEntity?.time)
                putExtra("EXTRA_AM_PM", alarmEntity?.amPm)
                putExtra("EXTRA_ALARM_NAME", alarmEntity?.label)
                putExtra("EXTRA_IS_ALARM_ENABLED", alarmEntity?.isEnabled ?: false)
                putExtra("EXTRA_IS_SOUND_AND_VIBRATION", alarmEntity?.isSoundAndVibration ?: false)
                putExtra("EXTRA_ALARM_ID", id)
            }
            requestSetAlarm.launch(intent)
        }
    }

    private fun updateAlarmBox(time: String?, amPm: String?, label: String?) {
        val alarmBox = binding.root.findViewById<CardView>(lastClickedAlarmBox)
        val textViewIds = getTextViewIdsForBox(lastClickedAlarmBox)

        val timeTextView = alarmBox.findViewById<TextView>(textViewIds.first)
        val labelTextView = alarmBox.findViewById<TextView>(textViewIds.second)
        val amPmTextView = alarmBox.findViewById<TextView>(textViewIds.third)

        timeTextView.typeface = ResourcesCompat.getFont(this, R.font.oxygen_bold)
        val (newTime, amPmText) = splitTimeAndAmPm(time)
        timeTextView.text = newTime
        amPmTextView.text = amPmText
        labelTextView.text = label
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
