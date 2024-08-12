package com.example.hicure.alarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hicure.R
import com.example.hicure.databinding.ActivitySetAlarmBinding
import java.util.Calendar

class SetAlarm : AppCompatActivity() {

    private lateinit var binding: ActivitySetAlarmBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: CustomTimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = binding.customTimePicker

        val initialTime = intent.getStringExtra("EXTRA_ALARM_TIME")
        val amPm = intent.getStringExtra("EXTRA_AM_PM")
        val boxDrawableResId = intent.getIntExtra("EXTRA_BOX_COLOR", R.drawable.set_alarm_box_blue)
        val switchDrawableResId =
            intent.getIntExtra("EXTRA_SWITCH_COLOR", R.drawable.alarm_switch_track_on_blue)
        val buttonDrawableResId =
            intent.getIntExtra("EXTRA_BUTTON_COLOR", R.drawable.set_alarm_save_button_box_blue)
        val alarmName = intent.getStringExtra("EXTRA_ALARM_NAME")
        val isAlarmEnabled = intent.getBooleanExtra("EXTRA_IS_ALARM_ENABLED", false)
        val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", 0)
        val isSoundAndVibration = intent.getBooleanExtra("EXTRA_IS_SOUND_AND_VIBRATION", false)

        val boxDrawable = ContextCompat.getDrawable(this, boxDrawableResId)
        val switchDrawable = ContextCompat.getDrawable(this, switchDrawableResId)
        val buttonDrawable = ContextCompat.getDrawable(this, buttonDrawableResId)

        binding.alarmBox.background = boxDrawable
        binding.boxLayout.background = boxDrawable
        binding.soundVibrationSwitch.trackDrawable = switchDrawable
        binding.saveButton.background = buttonDrawable

        if (initialTime != null) {
            timePicker.setSelectedTime(initialTime)
        }
        if (alarmName != null) {
            binding.alarmNameEditText.setText(alarmName)
        }
        binding.soundVibrationSwitch.isChecked = isSoundAndVibration

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.saveButton.setOnClickListener {
            try {
                // timePicker에서 시간이 정상적으로 선택되었는지 확인
                val selectedTime = timePicker.getSelectedTime()
                if (selectedTime == null) {
                    // 선택된 시간이 없으면 경고 메시지를 띄우고 종료
                    Toast.makeText(this, "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val alarmName = binding.alarmNameEditText.text.toString()
                val isSoundAndVibration = binding.soundVibrationSwitch.isChecked

                // RoomDB에 알람 설정 저장
                saveAlarmSettings(selectedTime)

                if (isAlarmEnabled) {
                    scheduleAlarm(selectedTime)
                } else {
                    cancelAlarm()
                }

                // 결과 인텐트에 필요한 데이터를 담아 설정
                val resultIntent = Intent().apply {
                    putExtra("EXTRA_SELECTED_TIME", selectedTime)
                    putExtra("EXTRA_ALARM_NAME", alarmName)
                    putExtra("EXTRA_BOX_COLOR", boxDrawableResId)
                    putExtra("EXTRA_AM_PM", amPm)
                    putExtra("EXTRA_IS_ALARM_ENABLED", isAlarmEnabled)
                    putExtra("EXTRA_IS_SOUND_AND_VIBRATION", isSoundAndVibration)
                    putExtra("EXTRA_ALARM_ID", alarmId)
                }

                // 정상적인 결과로 설정하고 Activity 종료
                setResult(Activity.RESULT_OK, resultIntent)
                finish()

            } catch (e: Exception) {
                // 예외 발생 시 로그 출력 및 사용자에게 오류 메시지 표시
                Log.e("Exception", "Database Error", e)
                Toast.makeText(this, "알람 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

        private fun saveAlarmSettings(time: String) {
            getSharedPreferences("alarm_prefs", MODE_PRIVATE).edit().apply {
                putString("alarm_time", time)
                apply()
            }
        }

        @SuppressLint("ScheduleExactAlarm")
        private fun scheduleAlarm(time: String) {
            val calendar = Calendar.getInstance()
            val hourMinute = time.split(" ")[0]
            val hour = hourMinute.split(":")[0].toInt()
            val minute = hourMinute.split(":")[1].toInt()
            val amPm = time.split(" ")[1]

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
                putExtra("time", time)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        @SuppressLint("UnspecifiedImmutableFlag")
        private fun cancelAlarm() {
            val intent = Intent(this, AlertReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
            }
        }
    }
