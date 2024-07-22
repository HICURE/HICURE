package com.example.hicure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hicure.databinding.ActivitySurveyBinding
import com.example.hicure.databinding.SurveyQuestionBinding
import java.text.SimpleDateFormat

class CustomAdapter: RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<QuestionMemo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = SurveyQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun getQCount():Int{
        return listData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo = listData.get(position)
        holder.setMemo(memo)
    }
}

class Holder(val binding: SurveyQuestionBinding): RecyclerView.ViewHolder(binding.root){
    fun setMemo(memo: QuestionMemo){
        binding.qNo.text = "${memo.no}."
        binding.qtext.text = memo.title
    }
}