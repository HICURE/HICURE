package com.example.hicure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityNewMeasumeBinding

class NewMeasume : AppCompatActivity() {

    private val binding: ActivityNewMeasumeBinding by lazy {
        ActivityNewMeasumeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }

}
