package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
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

        binding.surveyTitle.text = "만족도조사"
        binding.q1.questionText.text = "1. 기기가 제대로 작동하였나요?"
        binding.q2.questionText.text = "2. 기기 사용에 어려움은 없었나요?"
        binding.q5.text = "3. 하루 중 이상활동은 없었나요?"

        if (binding.surveyTitle.text == "진단평가") {

            binding.subTitle.visibility = View.GONE
            binding.line.visibility = View.GONE
            binding.underAppTItle.visibility = View.GONE
        }
    }
}