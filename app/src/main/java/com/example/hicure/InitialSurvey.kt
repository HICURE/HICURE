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
import com.example.hicure.utils.FirebaseCheckDate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InitialSurvey : AppCompatActivity() {

    val binding: ActivitySurveyBinding by lazy { ActivitySurveyBinding.inflate(layoutInflater) }
    lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadData { data ->
            adapter = CustomAdapter()
            adapter.listData = data
            adapter.selectedAnswers = MutableList(data.size) { null }

            binding.questionView.adapter = adapter
            binding.questionView.layoutManager = LinearLayoutManager(this)
        }

        "오늘의 폐건강".also { binding.actionTitle.text = it }

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

        binding.checkButton.setOnClickListener {
            val heightText = binding.userHeight.text.toString()

            // 빈칸인지 확인
            if (heightText.isBlank()) {
                Toast.makeText(this, "키를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 입력된 값이 Int인지 확인
            val heightValue = heightText.toIntOrNull()
            if (heightValue == null) {
                Toast.makeText(this, "키는 숫자여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // heightValue가 유효한 숫자인 경우 설문 제출 진행
            submitSurvey()
        }

        binding.surveyTitle.text = "진단평가"
        val referenceDate = LocalDate.now()
        binding.subTitle.text =
            referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        binding.line.visibility = View.GONE
        binding.underAppTItle.visibility = View.GONE

        binding.etc.text = "하루 중 이상활동은 없었나요? (필수 X)"
    }


    private fun loadData(callback: (MutableList<QuestionMemo>) -> Unit) {
        val data: MutableList<QuestionMemo> = mutableListOf()
        val database = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/")
        val surveyRef = database.getReference("InitialSurvey")

        surveyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (questionSnapshot in snapshot.children) {
                    val key = questionSnapshot.key?.toIntOrNull()
                    val title = questionSnapshot.getValue(String::class.java)
                    if (key != null && title != null) {
                        val memo = QuestionMemo(key, title)
                        data.add(memo)
                    }
                }
                callback(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InitialSurvey", "Failed to load survey questions", error.toException())
                callback(data) // Return empty or partially filled data on failure
            }
        })
    }

    private fun getAnswerText(checkedId: Int?): String {
        return when (checkedId) {
            R.id.yesButton -> "예"
            R.id.noButton -> "아니요"
            else -> "Unanswered"
        }
    }

    private fun submitSurvey() {
        if (adapter.allQuestionsAnswered()) {
            val surveyData = SurveyData().apply {
                answers = adapter.selectedAnswers.mapIndexed { index, checkedId ->
                    val question = adapter.listData[index].title
                    val safeKey = question.replace(".", "")
                        .replace("#", "")
                        .replace("$", "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "_") // 공백을 언더스코어로 대체
                    safeKey to getAnswerText(checkedId)
                }.toMap()

                answers = answers + ("기타" to binding.editText.text.toString())

                val now = LocalDateTime.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                date = now.format(dateFormatter)
                time = now.format(timeFormatter)

                Log.d("InitialSurvey", "Survey Answers: $answers")
            }

            val surveyResult = SurveyResult().apply {
                answers = mapOf(
                    "InitialSurvey" to surveyData
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
                Log.d("InitialSurvey", "Survey status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("InitialSurvey", "Failed to update survey status", e)
            }

        userRef.child("surveyResult").setValue(surveyResult.answers)
            .addOnSuccessListener {
                Log.d("InitialSurvey", "Survey results saved successfully.")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("InitialSurvey", "Failed to save survey results", e)
            }

        val now = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        userRef.child("startDate").setValue(now.format(dateFormatter))

        userRef.child("height").setValue(binding.userHeight.text.toString().toIntOrNull())
    }
}
