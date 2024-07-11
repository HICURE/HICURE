package com.example.hicure

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hicure.databinding.ActivityAppStartBinding

class AppStart : AppCompatActivity() {

    val binding: ActivityAppStartBinding by lazy {ActivityAppStartBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}