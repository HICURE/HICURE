package com.example.hicure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hicure.databinding.SurveyQuestionBinding

class CustomAdapter : RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<QuestionMemo>()
    var selectedAnswers = MutableList(listData.size) { null as Int? }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            SurveyQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo = listData[position]
        holder.setMemo(memo, selectedAnswers)
    }

    fun allQuestionsAnswered(): Boolean {
        return selectedAnswers.all { it != null }
    }
}

class Holder(val binding: SurveyQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setMemo(memo: QuestionMemo, selectedAnswers: MutableList<Int?>) {
        binding.qNo.text = "${memo.no}."
        binding.qtext.text = memo.title

        binding.RadioGroup.clearCheck()
        binding.RadioGroup.setOnCheckedChangeListener(null)

        val checkedId = selectedAnswers[adapterPosition]
        if (checkedId != null) {
            binding.RadioGroup.check(checkedId)
        }

        binding.RadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedAnswers[adapterPosition] = checkedId
        }
    }
}


