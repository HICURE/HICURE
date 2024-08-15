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

class ActivityImportanceOfLungCapacity : AppCompatActivity() {
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

        binding.content2.visibility = View.GONE
        binding.content3.visibility = View.GONE
        binding.content4.visibility = View.GONE
        binding.content5.visibility = View.GONE
        binding.image.visibility = View.GONE

        binding.content1.text = "<< 운동 성능 향상 >>\n충분한 산소 공급은 운동 시 지구력과 성능을 향상시킴.\n\n" +
                "<< 질병 예방 >>\n폐활량이 좋으면 호흡기 질환(천식, COPD 등) 예방에 도움이 됨.\n\n" +
                "<< 심혈관 건강 >>\n산소 공급이 원활하면 심혈관 질환의 위험이 감소함.\n\n" +
                "<< 에너지 수준 >>\n효율적인 가스 교환은 전신의 에너지 수준을 높여줌.\n\n" +
                "<< 삶의 질 향상 >>\n좋은 폐 기능은 일상 생활에서의 피로감을 줄이고 활동성을 높임."

        binding.backBtn.setOnClickListener{
            finish()
        }
    }
}