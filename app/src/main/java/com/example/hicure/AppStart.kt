package com.example.hicure

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
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

        // Splash Screen
        /*Handler().postDelayed(Runnable {
            val i = Intent(this@AppStart,MainActivity::class.java)
            startActivity(i)
            finish()
        }, 5000)*/
    }

    // Touch Screen
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action){
            MotionEvent.ACTION_DOWN -> {
                val i = Intent(this@AppStart,MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
        return super.onTouchEvent(event)
    }

    // If id is not checked, go to the check-id-activity



    // If id is checked, go to the main-activity

}