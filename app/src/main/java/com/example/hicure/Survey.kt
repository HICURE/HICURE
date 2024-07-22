package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hicure.databinding.ActivitySurveyBinding

class Survey : AppCompatActivity() {

    val binding: ActivitySurveyBinding by lazy { ActivitySurveyBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        "오늘의 폐건강".also { binding.actionTitle.text = it }

        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val actionTextWidth = binding.actionTitle.width

                binding.actionTitle.width = actionTextWidth + 10

                // binding.mainText.text = "$actionTextWidth"

                val layoutParams = binding.behindTitle.layoutParams
                layoutParams.width = actionTextWidth + 30
                binding.behindTitle.layoutParams = layoutParams

            }
        })

        binding.checkButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.surveyTitle.text="진단평가"
        binding.q1.questionText.text="1. 반복적으로 쌕쌕거리는 숨소리(천명음)"
        binding.q2.questionText.text="2. 호흡 곤란, 기침"
        binding.q3.questionText.text="3. 밤이나 새벽에 악화되는 증상"
        binding.q4.questionText.text="4. 운동 후 심해지는 천명이나 기침"
        binding.q5.text="하루 중 이상활동은 없었나요?"
    }
}