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
import kotlin.math.abs

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
        binding.backB.visibility = View.GONE
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

        val userHeight = binding.userHeight.text.toString().toIntOrNull()

        val userAge = sharedPreferences.getInt("user_age", 0)
        val userGender = sharedPreferences.getString("user_gender", null) ?: return

        val referenceValue = getReferenceValue(userGender, userHeight ?: 0, userAge)

        userRef.child("height").setValue(userHeight)
        userRef.child("referenceValue").setValue(referenceValue)

        with(sharedPreferences.edit()) {
            userHeight?.let { putInt("user_height", it) }
            referenceValue?.let { putInt("reference_value", it) }
            apply()
        }
    }
    private fun getReferenceValue(gender: String, height: Int, age: Int): Int {
        val heightsMale = listOf(122, 130, 137, 145, 152, 160, 168, 175, 183, 191)
        val heightsFemale = listOf(114, 122, 130, 137, 145, 152, 160, 168, 175, 183, 191)
        val ages = listOf(8, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75)

        val valuesMale = listOf(
            listOf(178, 191, 253, 354, 361, 364, 364, 359, 351, 339, 323, 302, 278, 251, 219),
            listOf(206, 220, 281, 383, 390, 393, 392, 388, 380, 367, 352, 331, 307, 280, 248),
            listOf(237, 251, 312, 414, 423, 424, 423, 419, 410, 398, 382, 362, 338, 310, 278),
            listOf(269, 283, 344, 445, 452, 456, 455, 451, 442, 430, 414, 394, 370, 342, 310),
            listOf(299, 313, 374, 476, 483, 486, 486, 485, 476, 464, 448, 428, 404, 376, 344),
            listOf(339, 352, 414, 515, 522, 525, 525, 520, 512, 500, 484, 463, 439, 412, 380),
            listOf(371, 383, 446, 559, 566, 569, 568, 563, 554, 541, 521, 501, 477, 449, 417),
            listOf(415, 429, 491, 604, 611, 614, 613, 607, 597, 589, 576, 554, 530, 501, 469),
            listOf(456, 470, 531, 632, 640, 643, 642, 638, 629, 617, 601, 581, 557, 529, 497),
            listOf(499, 512, 574, 675, 682, 685, 685, 680, 672, 659, 644, 623, 599, 572, 540)
        )

        val valuesFemale = listOf(
            listOf(155, 191, 247, 260, 271, 271, 269, 263, 255, 243, 229, 211, 190, 166, 147),
            listOf(175, 212, 268, 280, 291, 292, 289, 284, 275, 263, 248, 230, 209, 185, 168),
            listOf(203, 233, 297, 309, 320, 312, 313, 306, 296, 284, 270, 252, 231, 207, 189),
            listOf(220, 256, 311, 324, 331, 335, 333, 328, 319, 307, 293, 275, 254, 230, 207),
            listOf(244, 280, 335, 348, 355, 359, 357, 352, 343, 331, 317, 299, 278, 254, 230),
            listOf(266, 305, 361, 374, 380, 384, 383, 377, 365, 342, 326, 306, 279, 254, 230),
            listOf(295, 332, 388, 400, 407, 411, 409, 403, 395, 383, 368, 350, 329, 305, 285),
            listOf(325, 360, 415, 428, 435, 439, 437, 431, 423, 411, 397, 379, 358, 334, 313),
            listOf(353, 389, 445, 457, 464, 468, 466, 461, 452, 440, 425, 407, 387, 362, 342),
            listOf(383, 419, 475, 485, 494, 499, 497, 491, 482, 471, 456, 438, 417, 393, 372),
            listOf(415, 451, 507, 520, 526, 530, 531, 523, 514, 503, 488, 470, 449, 425, 407)
        )

        val heights = if (gender == "남성") heightsMale else heightsFemale
        val values = if (gender == "남성") valuesMale else valuesFemale

        if (gender == "남성" || gender == "여성") {
            // 키와 나이에 가장 가까운 값을 찾기
            val nearestHeight = heights.minWithOrNull(compareBy({ abs(it - height) }, { it })) ?: heights.first()
            val nearestAge = ages.minWithOrNull(compareBy({ abs(it - age) }, { it })) ?: ages.first()

            val heightIndex = heights.indexOf(nearestHeight)
            val ageIndex = ages.indexOf(nearestAge)

            return values[heightIndex][ageIndex]
        } else {
            // 남성과 여성 각각의 reference value를 구하고 그 평균값을 반환
            val nearestHeightMale = heightsMale.minByOrNull { abs(it - height) } ?: heightsMale.first()
            val nearestAgeMale = ages.minByOrNull { abs(it - age) } ?: ages.first()

            val nearestHeightFemale = heightsFemale.minByOrNull { abs(it - height) } ?: heightsFemale.first()
            val nearestAgeFemale = ages.minByOrNull { abs(it - age) } ?: ages.first()

            val maleIndexHeight = heightsMale.indexOf(nearestHeightMale)
            val maleIndexAge = ages.indexOf(nearestAgeMale)
            val femaleIndexHeight = heightsFemale.indexOf(nearestHeightFemale)
            val femaleIndexAge = ages.indexOf(nearestAgeFemale)

            val maleValue = valuesMale[maleIndexHeight][maleIndexAge]
            val femaleValue = valuesFemale[femaleIndexHeight][femaleIndexAge]

            return (maleValue + femaleValue) / 2
        }
    }
}
