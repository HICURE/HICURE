package com.example.hicure

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlarmList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)

        val alarmBoxBlue = findViewById<CardView>(R.id.alarmBoxBlue)
        val alarmBoxYellow = findViewById<CardView>(R.id.alarmBoxYellow)
        val alarmBoxPink = findViewById<CardView>(R.id.alarmBoxPink)

        alarmBoxBlue.setOnClickListener {
            navigateToSetAlarm()
        }

        alarmBoxYellow.setOnClickListener {
            navigateToSetAlarm()
        }

        alarmBoxPink.setOnClickListener {
            navigateToSetAlarm()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun navigateToSetAlarm() {
        val intent = Intent(this, SetAlarm::class.java)
        startActivity(intent)
    }
}