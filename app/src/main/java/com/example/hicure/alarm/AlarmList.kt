package com.example.hicure.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.R
import com.example.hicure.databinding.ActivityAlarmListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmList : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmListBinding
    private lateinit var requestSetAlarm: ActivityResultLauncher<Intent>
    private var lastClickedAlarmBox: Int = 0
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        // Initialize repository
        val database = AlarmDatabase.getInstance(applicationContext)
        alarmRepository = AlarmRepository(database.alarmDao())

        initializeDefaultAlarms()
        displayExistingAlarms()
        setupAlarmBoxListeners()
        setupSwitchListeners()
        initActivityResultLauncher()
        // 앱 실행 시 알람 정보 초기화 코드
//        CoroutineScope(Dispatchers.IO).launch {
//            alarmRepository.clearAllAlarms() // Clear the database
//            initializeDefaultAlarms() // Initialize default alarms
//        }

        "알람".also { binding.actionTitle.text = it }

        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val actionTextWidth = binding.actionTitle.width

                binding.actionTitle.width = actionTextWidth + 10

                val layoutParams = binding.behindTitle.layoutParams
                layoutParams.width = actionTextWidth + 30
                binding.behindTitle.layoutParams = layoutParams
            }
        })
    }

    private fun initializeDefaultAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if alarms are already set
            val existingAlarms = alarmRepository.getAllAlarms.first()
            if (existingAlarms.isEmpty()) {
                // Insert default alarms
                val defaultAlarms = listOf(
                    AlarmEntity(id = 1, time = "12:36 PM", amPm = "", label = "아침", isEnabled = false, isSoundAndVibration = false),
                    AlarmEntity(id = 2, time = "12:37 PM", amPm = "", label = "점심", isEnabled = false, isSoundAndVibration = false),
                    AlarmEntity(id = 3, time = "12:38 PM", amPm = "", label = "저녁", isEnabled = false, isSoundAndVibration = false)
                )
                defaultAlarms.forEach { alarm ->
                    alarmRepository.insertAlarm(alarm)
                }
                displayExistingAlarms()
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

    private fun displayExistingAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val alarms = alarmRepository.getAllAlarms.first()
            runOnUiThread {
                alarms.forEach { alarm ->

                    when (alarm.id) {
                        1 -> {
                            val (initialTime, initialAMPM) = splitTimeAndAmPm(alarm.time)
                            binding.alarmTimeBlue.text = getString(R.string.alarm_box_blue_time, initialTime)
                            binding.alarmAmPmBlue.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelBlue.text = getString(R.string.alarm_box_name_blue, alarm.label)
                        }
                        2 -> {
                            val (initialTime, initialAMPM) = splitTimeAndAmPm(alarm.time)
                            binding.alarmTimeYellow.text = getString(R.string.alarm_box_yellow_time, initialTime)
                            binding.alarmAmPmYellow.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelYellow.text = getString(R.string.alarm_box_name_yellow, alarm.label)
                        }
                        3 -> {
                            val (initialTime, initialAMPM) = splitTimeAndAmPm(alarm.time)
                            binding.alarmTimePink.text = getString(R.string.alarm_box_pink_time, initialTime)
                            binding.alarmAmPmPink.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelPink.text = getString(R.string.alarm_box_name_pink, alarm.label)
                        }
                    }
                    updateSwitchState(alarm)
                }
            }
        }
    }


    private fun updateSwitchState(alarm: AlarmEntity) {
        when (alarm.id) {
            1 -> binding.alarmSwitchBlue.isChecked = alarm.isEnabled
            2 -> binding.alarmSwitchYellow.isChecked = alarm.isEnabled
            3 -> binding.alarmSwitchPink.isChecked = alarm.isEnabled
        }
    }

    private fun setupSwitchListeners() {
        binding.alarmSwitchBlue.setOnCheckedChangeListener { _, isChecked ->
            updateAlarmStatus(1, isChecked)
        }

        binding.alarmSwitchYellow.setOnCheckedChangeListener { _, isChecked ->
            updateAlarmStatus(2, isChecked)
        }

        binding.alarmSwitchPink.setOnCheckedChangeListener { _, isChecked ->
            updateAlarmStatus(3, isChecked)
        }
    }

    private fun updateAlarmStatus(alarmId: Int, isEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                it.isEnabled = isEnabled
                alarmRepository.insertOrUpdateAlarm(it)
                if (isEnabled) {
                    scheduleAlarm(it)
                } else {
                    cancelAlarm(it)
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(alarm: AlarmEntity) {
        val calendar = Calendar.getInstance()
        val hourMinute = alarm.time.split(" ")[0]
        val hour = hourMinute.split(":")[0].toInt()
        val minute = hourMinute.split(":")[1].toInt()
        val amPm = alarm.amPm

        if (amPm.equals("PM", ignoreCase = true) && hour < 12) {
            calendar.set(Calendar.HOUR_OF_DAY, hour + 12)
        } else if (amPm.equals("AM", ignoreCase = true) && hour == 12) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
        }
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, AlertReceiver::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun cancelAlarm(alarm: AlarmEntity) {
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
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

                    updateAlarmBox(time, label)
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

    private fun updateAlarmBox(time: String?, label: String?) {
        // ViewBinding을 통해 CardView를 참조
        val alarmBox = when (lastClickedAlarmBox) {
            R.id.alarmBoxBlue -> binding.alarmBoxBlue
            R.id.alarmBoxYellow -> binding.alarmBoxYellow
            R.id.alarmBoxPink -> binding.alarmBoxPink
            else -> null
        }

        if (alarmBox == null) {
            // Handle case where alarmBox is null
            return
        }

        val textViewIds = getTextViewIdsForBox(lastClickedAlarmBox)

        val timeTextView = alarmBox.findViewById<TextView>(textViewIds.first)
        val labelTextView = alarmBox.findViewById<TextView>(textViewIds.second)
        val amPmTextView = alarmBox.findViewById<TextView>(textViewIds.third)

        if (timeTextView == null || labelTextView == null || amPmTextView == null) {
            // Handle case where any of the TextViews are null
            return
        }

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
