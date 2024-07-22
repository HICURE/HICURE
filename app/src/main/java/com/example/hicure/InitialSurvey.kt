package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hicure.databinding.ActivitySurveyBinding

class InitialSurvey : AppCompatActivity() {

    val binding: ActivitySurveyBinding by lazy { ActivitySurveyBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data: MutableList<QuestionMemo> = loadData()
        var adapter = CustomAdapter()
        adapter.listData = data
        binding.questionView.adapter = adapter

        binding.questionView.layoutManager = LinearLayoutManager(this)

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

        binding.surveyTitle.text = "진단평가"

        if (binding.surveyTitle.text == "진단평가") {
            binding.subTitle.visibility = View.GONE
            binding.line.visibility = View.GONE
            binding.underAppTItle.visibility = View.GONE
        }

        binding.q5.text = "하루 중 이상활동은 없었나요?"

    }
}

private fun loadData(): MutableList<QuestionMemo> {
    val data: MutableList<QuestionMemo> = mutableListOf()

    val surveyQuestion = listOf(
        "반복적으로 쌕쌕거리는 숨소리(천명음)",
        "호흡 곤란, 기침",
        "밤이나 새벽에 악화되는 증상",
        "운동 후 심해지는 천명이나 기침"
    )

    surveyQuestion.forEachIndexed { index, title ->
        val no = index + 1
        val memo = QuestionMemo(no, title)
        data.add(memo)
    }
    return data;
}
