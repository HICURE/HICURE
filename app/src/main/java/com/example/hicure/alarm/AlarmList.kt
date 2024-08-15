package com.example.hicure.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.MainActivity
import com.example.hicure.R
import com.example.hicure.UserInfo
import com.example.hicure.databinding.ActivityAlarmListBinding
import com.example.hicure.serveinfo.ServeInfo
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

        binding.bnMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_Home -> startNewActivity(MainActivity::class.java)
                R.id.ic_Alarm -> startNewActivity(AlarmList::class.java)
                R.id.ic_Serve -> startNewActivity(ServeInfo::class.java)
                R.id.ic_User -> startNewActivity(UserInfo::class.java)
            }
            true
        }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun initializeDefaultAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingAlarms = alarmRepository.getAllAlarms.first()
            if (existingAlarms.isEmpty()) {
                val defaultAlarms = listOf(
                    AlarmEntity(id = 1, time = "05:00 AM", amPm = "", label = "아침", isEnabled = false, isSoundAndVibration = false),
                    AlarmEntity(id = 2, time = "05:01 AM", amPm = "", label = "점심", isEnabled = false, isSoundAndVibration = false),
                    AlarmEntity(id = 3, time = "05:02 AM", amPm = "", label = "저녁", isEnabled = false, isSoundAndVibration = false)
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
                    val (initialTime, initialAMPM) = splitTimeAndAmPm(alarm.time)
                    when (alarm.id) {
                        1 -> {
                            binding.alarmTimeBlue.text = getString(R.string.alarm_box_blue_time, initialTime)
                            binding.alarmAmPmBlue.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelBlue.text = getString(R.string.alarm_box_name_blue, alarm.label)
                            binding.alarmSwitchBlue.isChecked = alarm.isEnabled
                        }
                        2 -> {
                            binding.alarmTimeYellow.text = getString(R.string.alarm_box_yellow_time, initialTime)
                            binding.alarmAmPmYellow.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelYellow.text = getString(R.string.alarm_box_name_yellow, alarm.label)
                            binding.alarmSwitchYellow.isChecked = alarm.isEnabled
                        }
                        3 -> {
                            binding.alarmTimePink.text = getString(R.string.alarm_box_pink_time, initialTime)
                            binding.alarmAmPmPink.text = getString(R.string.alarm_box_AMPM, initialAMPM)
                            binding.alarmLabelPink.text = getString(R.string.alarm_box_name_pink, alarm.label)
                            binding.alarmSwitchPink.isChecked = alarm.isEnabled
                        }
                    }
                    updateSwitchState(alarm)
                }
            }
        }
    }

    private fun updateAlarmStatus(alarmId: Int, isEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                it.isEnabled = isEnabled
                alarmRepository.insertOrUpdateAlarm(it)
                Log.d("AlarmList", "Alarm ${it.id} status updated to ${it.isEnabled}")

                if (isEnabled) {
                    scheduleAlarm(it)
                } else {
                    cancelAlarm(it)
                }
            }
            // Re-schedule all enabled alarms to ensure they are set properly
            scheduleAllEnabledAlarms()
        }
    }

    private fun scheduleAllEnabledAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val alarms = alarmRepository.getAllAlarms.first()
            alarms.forEach { alarm ->
                if (alarm.isEnabled) {
                    scheduleAlarm(alarm)
                } else {
                    cancelAlarm(alarm)
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


    @SuppressLint("ObsoleteSdkInt", "ScheduleExactAlarm")
    private fun scheduleAlarm(alarm: AlarmEntity) {
        val calendar = Calendar.getInstance().apply {
            // 사용자가 설정한 시간과 AM/PM에 따라 시간을 설정합니다.
            val hourMinute = alarm.time.split(" ")[0]
            val hour = hourMinute.split(":")[0].toInt()
            val minute = hourMinute.split(":")[1].toInt()
            val amPm = alarm.amPm

            if (amPm.equals("PM", ignoreCase = true) && hour < 12) {
                set(Calendar.HOUR_OF_DAY, hour + 12)
            } else if (amPm.equals("AM", ignoreCase = true) && hour == 12) {
                set(Calendar.HOUR_OF_DAY, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // 현재 시간보다 이전이면, 다음 날로 설정
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

        Log.d("AlarmList", "Setting alarm ${alarm.id} for ${calendar.timeInMillis}")

        // 매일 같은 시간에 알람이 울리도록 설정합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
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
            Log.d("AlarmList", "Cancelled alarm ${alarm.id}")
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
                        runOnUiThread {
                            updateAlarmBox(time, label)
                        }
                    }
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
        val alarmBox = when (lastClickedAlarmBox) {
            R.id.alarmBoxBlue -> binding.alarmBoxBlue
            R.id.alarmBoxYellow -> binding.alarmBoxYellow
            R.id.alarmBoxPink -> binding.alarmBoxPink
            else -> null
        }

        if (alarmBox == null) {
            return
        }

        val textViewIds = getTextViewIdsForBox(lastClickedAlarmBox)

        val timeTextView = alarmBox.findViewById<TextView>(textViewIds.first)
        val labelTextView = alarmBox.findViewById<TextView>(textViewIds.second)
        val amPmTextView = alarmBox.findViewById<TextView>(textViewIds.third)

        if (timeTextView == null || labelTextView == null || amPmTextView == null) {
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
