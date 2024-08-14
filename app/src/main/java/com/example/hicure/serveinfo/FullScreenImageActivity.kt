package com.example.hicure.serveinfo

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.R

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView: ImageView = findViewById(R.id.fullScreenImageView)

        val imageResId = intent.getIntExtra("imageResId", R.drawable.image1)
        imageView.setImageResource(imageResId)

        imageView.setOnClickListener {
            finish()
        }
    }
}