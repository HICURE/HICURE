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

    private val connectionStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra("status") ?: "Unknown"
            val deviceName = intent.getStringExtra("device_name") ?: "Unknown Device"
            val deviceAddress = intent.getStringExtra("device_address") ?: "Unknown Address"
            updateConnectionStatus(status, deviceName, deviceAddress)
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val deviceName = intent.getStringExtra("device_name") ?: "Unknown Device"
        val deviceAddress = intent.getStringExtra("device_address") ?: "Unknown Address"

        binding.bleName.text = deviceName
        binding.bleAddress.text = deviceAddress

        val filter = IntentFilter("com.example.hicure.CONNECTION_STATUS")
        registerReceiver(connectionStatusReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectionStatusReceiver)
    }

    private fun updateConnectionStatus(status: String, deviceName: String, deviceAddress: String) {
        binding.bleName.text = deviceName
        binding.bleAddress.text = deviceAddress
        binding.connectStat.text = if (status == "Connected") {
            "연결되어 있음"
        } else {
            "연결되어 있지 않음"
        }
    }
}
