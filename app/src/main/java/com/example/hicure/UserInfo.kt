package com.example.hicure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityUserInfoBinding
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var userRef: DatabaseReference

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

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null) ?: return

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

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

        userRef.child("startDate").get().addOnSuccessListener { snapshot ->
            val startDateString = snapshot.getValue(String::class.java)
            val baseDate: Date = startDateString?.let {
                try {
                    sdf.parse(it) ?: Date()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse startDate", e)
                    Date()
                }
            } ?: Date()

            val currentDate = Calendar.getInstance().time

            userRef.child("surveyResult").get().addOnSuccessListener { snapshot ->
                val surveyResults = snapshot.value as? Map<String, Any>
                val firstSurveyState = getSurveyState(baseDate, currentDate, 0, surveyResults, "Survey 1")
                val secondSurveyState = getSurveyState(baseDate, currentDate, 3, surveyResults, "Survey 2")
                val thirdSurveyState = getSurveyState(baseDate, currentDate, 7, surveyResults, "Survey 3")
                val fourthSurveyState = getSurveyState(baseDate, currentDate, 14, surveyResults, "Survey 4")

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

                binding.surveys.firstSurvey.setOnClickListener { onSurveyClick(firstSurveyState, "1") }
                binding.surveys.secondSurvey.setOnClickListener { onSurveyClick(secondSurveyState, "2") }
                binding.surveys.thirdSurvey.setOnClickListener { onSurveyClick(thirdSurveyState, "3") }
                binding.surveys.lastSurvey.setOnClickListener { onSurveyClick(fourthSurveyState, "4") }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to get survey results", e)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to get startDate", e)
        }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun getSurveyState(baseDate: Date, currentDate: Date, daysAfter: Int, surveyResults: Map<String, Any>?, surveyKey: String): SurveyState {
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, daysAfter)
        }
        return if (currentDate.after(calendar.time)) {
            if (surveyResults?.containsKey(surveyKey) == true) {
                SurveyState.DONE
            } else {
                SurveyState.CAN_DO
            }
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

    private fun onSurveyClick(state: SurveyState, surveyNumber: String) {
        when (state) {
            SurveyState.CAN_DO -> {

                val intent = Intent(this, Survey::class.java).apply {
                    putExtra("survey_number", surveyNumber)
                }
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
