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
import android.widget.FrameLayout
import com.example.hicure.databinding.ActivityAppStartBinding

class MainActivity : AppCompatActivity() {

    val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate((layoutInflater)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

}