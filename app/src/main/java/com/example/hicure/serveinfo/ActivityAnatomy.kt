package com.example.hicure.serveinfo

import android.content.Intent
import android.content.res.ColorStateList
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

class ActivityAnatomy : AppCompatActivity() {
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

        binding.subTitle.text = infoSubTitle

        binding.content2.visibility = View.GONE
        binding.content3.visibility = View.GONE
        binding.content4.visibility = View.GONE

        binding.content1.text = "폐는 호흡계의 주요 기관으로, 산소를 혈액으로 전달하고 이산화탄소를 외부로 배출하는 역할을 합니다. \n" +
                "\n" +
                "인간의 폐는 좌폐와 우폐로 나뉘어 있으며, 각각이 여러 엽으로 구성되어 있습니다. \n" +
                "\n" +
                "폐는 기관지를 통해 기도와 연결되며, 기관지는 세분화되어 세기관지로 나뉩니다. \n" +
                "\n" +
                "세기관지는 다시 폐포로 이어지며, 이곳에서 가스 교환이 이루어집니다."

        binding.content5.text = " >> 클릭해서 정보 보러가기 <<"
        binding.content5.setTextColor(resources.getColor(R.color.edge_blue, null))
        binding.content5.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.amc.seoul.kr/asan/healthinfo/body/bodyDetail.do?bodyId=64")
            }
            startActivity(intent)
        }

        binding.image.setOnClickListener {
            val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                putExtra("imageResId", R.drawable.image1) // 실제 이미지 리소스 ID를 사용
            }
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener{
            finish()
        }
    }
}