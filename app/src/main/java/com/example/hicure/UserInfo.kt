package com.example.hicure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityUserInfoBinding
import android.view.View
import android.widget.Toast
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

enum class SurveyState {
    DONE, CAN_DO, TO_DO
}

class UserInfo : AppCompatActivity() {

    val binding: ActivityUserInfoBinding by lazy { ActivityUserInfoBinding.inflate(layoutInflater) }
    private val TAG = "UserInfo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        "설정".also { binding.actionTitle.text = it }
        binding.bnMain.selectedItemId = R.id.ic_User

        binding.bnMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_Home -> startNewActivity(MainActivity::class.java)
                R.id.ic_Alarm -> startNewActivity(AlarmList::class.java)
                R.id.ic_Serve -> startNewActivity(ServeInfo::class.java)
                R.id.ic_User -> startNewActivity(UserInfo::class.java)
            }
            true
        }
        binding.bnMain.selectedItemId = R.id.ic_User

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userAge = sharedPreferences.getInt("user_age", 0)
        val userGender = sharedPreferences.getString("user_gender", null)

        binding.userState.userAge.text = userAge.toString()
        binding.userState.userGender.text = userGender ?: "N/A"

        binding.inquiry.inquiry.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://forms.gle/NGK7AnM6sddnqhKV9")
            startActivity(intent)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfShort = SimpleDateFormat("MM-dd", Locale.getDefault())

        val baseDate: Date = sdf.parse("2024-07-10") ?: Date()
        val currentDate = Calendar.getInstance().time

        val firstSurveyState = getSurveyState(baseDate, currentDate, 0)
        val secondSurveyState = getSurveyState(baseDate, currentDate, 3)
        val thirdSurveyState = getSurveyState(baseDate, currentDate, 7)
        val fourthSurveyState = getSurveyState(baseDate, currentDate, 14)

        val firstSurveyDate = getDateForDaysAfter(baseDate, 0)
        val secondSurveyDate = getDateForDaysAfter(baseDate, 3)
        val thirdSurveyDate = getDateForDaysAfter(baseDate, 7)
        val fourthSurveyDate = getDateForDaysAfter(baseDate, 14)

        binding.surveys.date1.text = sdfShort.format(firstSurveyDate)
        binding.surveys.date2.text = sdfShort.format(secondSurveyDate)
        binding.surveys.date3.text = sdfShort.format(thirdSurveyDate)
        binding.surveys.date4.text = sdfShort.format(fourthSurveyDate)


        setSurveyState(binding.surveys.firstSurvey, firstSurveyState, binding.surveys.Icon1)
        setSurveyState(binding.surveys.secondSurvey, secondSurveyState, binding.surveys.Icon2)
        setSurveyState(binding.surveys.thirdSurvey, thirdSurveyState, binding.surveys.Icon3)
        setSurveyState(binding.surveys.lastSurvey, fourthSurveyState, binding.surveys.Icon4)

        binding.surveys.firstSurvey.setOnClickListener { onSurveyClick(firstSurveyState) }
        binding.surveys.secondSurvey.setOnClickListener { onSurveyClick(secondSurveyState) }
        binding.surveys.thirdSurvey.setOnClickListener { onSurveyClick(thirdSurveyState) }
        binding.surveys.lastSurvey.setOnClickListener { onSurveyClick(fourthSurveyState) }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun getSurveyState(baseDate: Date, currentDate: Date, daysAfter: Int): SurveyState {
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, daysAfter)
        }
        return if (currentDate.after(calendar.time)) {
            SurveyState.CAN_DO
        } else {
            SurveyState.TO_DO
        }
    }

    private fun setSurveyState(view: View, state: SurveyState, icon: View) {

        when (state) {
            SurveyState.DONE -> {
                view.setBackgroundResource(R.drawable.yellow_frame)
                icon.setBackgroundResource(R.drawable.check)
            }
            SurveyState.CAN_DO -> {
                view.setBackgroundResource(R.drawable.pop_up_frame)
                icon.alpha = 0.0f
            }
            SurveyState.TO_DO -> {
                view.setBackgroundResource(R.drawable.gray_frame)
                icon.setBackgroundResource(R.drawable.lock)
            }
        }
    }

    private fun getDateForDaysAfter(baseDate: Date, daysAfter: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, daysAfter)
        }
        return calendar.time
    }

    private fun onSurveyClick(state: SurveyState) {
        when (state) {
            SurveyState.CAN_DO -> {
                val intent = Intent(this, Survey::class.java)
                startActivity(intent)
            }
            SurveyState.DONE -> {
                Toast.makeText(this,"이미 참여한 설문입니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Survey already done.")
            }
            SurveyState.TO_DO -> {
                Toast.makeText(this,"현재 참여할 수 없는 설문입니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Survey not available yet.")
            }
        }
    }
}
