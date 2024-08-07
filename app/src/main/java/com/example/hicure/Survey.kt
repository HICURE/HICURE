package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hicure.databinding.ActivitySurveyBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.Chronology
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Survey : AppCompatActivity() {

    val binding: ActivitySurveyBinding by lazy { ActivitySurveyBinding.inflate(layoutInflater) }
    lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data: MutableList<QuestionMemo> = loadData()
        val surveyNumber = intent.getStringExtra("survey_number") ?: "N"

        adapter = CustomAdapter().apply {
            listData = data
        }

        binding.questionView.layoutManager = LinearLayoutManager(this)
        binding.questionView.adapter = adapter

        "만족도조사".also { binding.actionTitle.text = it }

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
        binding.etc.text = "현재까지 진행함에 있어 느낀 점을 적어주세요."

        binding.checkButton.setOnClickListener {
            submitSurvey(surveyNumber)
        }

        if (binding.actionTitle.text=="만족도조사"){
            val currentDate = LocalDate.now()
            val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MM-dd"))
            binding.subTitle.text = "$formattedDate"
            binding.surveyTitle.text="$surveyNumber 일차 만족도조사"
            binding.backCircle.root.visibility = View.GONE
        }
    }
    private fun loadData(): MutableList<QuestionMemo> {
        val data: MutableList<QuestionMemo> = mutableListOf()

        val surveyQuestion = listOf(
            "기기가 제대로 작동하였나요?",
            "기기 사용에 어려움은 없었나요?"
        )

        surveyQuestion.forEachIndexed { index, title ->
            val no = index + 1
            val memo = QuestionMemo(no, title)
            data.add(memo)
        }
        return data;
    }

    private fun getAnswerText(checkedId: Int?): String {
        return when (checkedId) {
            R.id.yesButton -> "예"
            R.id.noButton -> "아니요"
            else -> "Unanswered"
        }
    }

    private fun submitSurvey(surveyNumber: String) {
        if (adapter.allQuestionsAnswered()) {
            val surveyData = SurveyData().apply {
                answers = adapter.selectedAnswers.mapIndexed { index, checkedId ->
                    (index + 1).toString() to getAnswerText(checkedId)
                }.toMap()

                answers = answers + ("기타" to binding.editText.text.toString())

                val now = LocalDateTime.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                date = now.format(dateFormatter)
                time = now.format(timeFormatter)

                Log.d("Survey", "Survey Answers: $answers")
            }

            val surveyResult = SurveyResult().apply {
                answers = mapOf(
                    "Survey $surveyNumber" to surveyData
                )
            }

            updateSurveyStatus(surveyResult)
        } else {
            Toast.makeText(this, "모든 항목이 체크되지 않았습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSurveyStatus(surveyResult: SurveyResult) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null) ?: return

        val userRef = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/")
            .getReference("users").child(userId)

        userRef.child("survey").setValue(true)
            .addOnSuccessListener {
                Log.d("Survey", "Survey status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Survey", "Failed to update survey status", e)
            }

        userRef.child("surveyResult").updateChildren(surveyResult.answers)
            .addOnSuccessListener {
                Log.d("Survey", "Survey results saved successfully.")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Survey", "Failed to save survey results", e)
            }

        val now = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        userRef.child("startDate").setValue(now.format(dateFormatter))
    }

}