package com.example.hicure

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hicure.R
import com.example.hicure.databinding.CardInfoBinding

class InfoAdapter(
    private val context: Context,
    private val items: List<InfoItem>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class InfoViewHolder(val binding: CardInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkButton.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val binding = CardInfoBinding.inflate(LayoutInflater.from(context), parent, false)
        return InfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            infoTitle.text = item.title
            content.text = item.content

            // Set selected state based on `visited`
            root.isSelected = item.visited
        }
    }
}
