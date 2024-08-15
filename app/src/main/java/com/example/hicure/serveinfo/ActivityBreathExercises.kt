package com.example.hicure.serveinfo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hicure.R
import com.example.hicure.databinding.InfoDetailsBinding

class ActivityBreathExercises : AppCompatActivity() {
    val binding: InfoDetailsBinding by lazy { InfoDetailsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val infoSubTitle = intent.getStringExtra("subTitle")
        val infoTitle = intent.getStringExtra("title")

        "$infoTitle".also { binding.actionTitle.text = it }

        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val actionTextWidth = binding.actionTitle.width
                binding.actionTitle.width = actionTextWidth + 10

                val layoutParams = binding.behindTitle.layoutParams
                layoutParams.width = actionTextWidth + 30
                binding.behindTitle.layoutParams = layoutParams
            }
        })
        binding.backB.setOnClickListener {
            startActivity(Intent(this, ServeInfo::class.java))
            finish()
        }
        binding.subTitle.text = infoSubTitle

        binding.image.visibility = View.GONE
        binding.content2.visibility = View.GONE
        binding.content3.visibility = View.GONE
        binding.content4.visibility = View.GONE
        binding.content5.visibility = View.GONE

        binding.content1.text = "<< 10대 >>\n 심폐 지구력을 높이는 운동이 중요. \n" +
                "조깅, 수영, 자전거 타기, 팀 스포츠(축구, 농구 등)를 추천.\n\n" +
                "<< 20대 >>\n다양한 심폐 지구력 운동이 적합함.\n" +
                "고강도 인터벌 트레이닝(HIIT), 마라톤, 수영, 사이클링 등을 추천.\n\n" +
                "<< 30대 >>\n일과 가정을 병행하면서 시간을 효율적으로 사용할 수 있는 운동이 필요.\n" +
                "조깅, 요가, 필라테스, 에어로빅 등을 추천.\n\n" +
                "<< 40대 >>\n유산소 운동과 근력 운동을 병행하는 것이 중요. \n" +
                "파워 워킹, 스피닝, 수영, 저강도 인터벌 트레이닝 등을 추천.\n\n" +
                "<< 50대 >>\n무릎이나 허리에 무리가 가지 않도록 저충격 운동을 권장함. \n" +
                "걷기, 수영, 저강도 에어로빅, 체조 등을 추천.\n\n" +
                "<< 60대 이상 >>\n유연성과 균형을 유지할 수 있는 운동이 필요함.\n" +
                "걷기, 수영, 가벼운 요가, 체조 등이 적합함."


        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}
