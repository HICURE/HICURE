package com.example.hicure

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hicure.databinding.ActivityMainBinding
import android.view.ViewTreeObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.FrameLayout
import com.example.hicure.databinding.ActivityAppStartBinding
import java.sql.Time
import java.sql.Timestamp

class MainActivity : AppCompatActivity() {

    val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate((layoutInflater)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentDate = getCurrentDate()
        val currentTime= getCurrentTime()

        val textViewDate: TextView = findViewById(R.id.textViewDate)
        textViewDate.text = currentDate
        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime

        "원하는 타이틀 입력".also { binding.actionTitle.text = it }

        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

               override fun onGlobalLayout(){
                    binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                   val actionTextWidth = binding.actionTitle.width

                   binding.actionTitle.width = actionTextWidth + 10

                   // binding.mainText.text = "$actionTextWidth"

                   val layoutParams = binding.behindTitle.layoutParams
                   layoutParams.width = actionTextWidth + 30
                   binding.behindTitle.layoutParams = layoutParams

               }
        })

    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return dateFormat.format(Date())
    }
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("hh : mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}