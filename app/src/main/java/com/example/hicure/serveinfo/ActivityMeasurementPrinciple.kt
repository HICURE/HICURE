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

class ActivityMeasurementPrinciple : AppCompatActivity() {
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

        binding.content5.visibility = View.GONE
        binding.image.visibility = View.GONE

        binding.content1.text = "폐활량은 폐의 최대 공기 흡입 및 배출 용량을 의미합니다.\n\n" +
                "이를 측정하는 방법 중 하나가\n피크플로우미터(Peak Flow Meter)입니다.\n"

        binding.content2.text = "<< 피크플로우미터 사용 방법 >>\n" +
                "1. 사용자는 깊게 숨을 들이쉰 후 피크플로우미터의 마우스피스에 입을 대고 가능한 한 빠르게 강하게 숨을 내쉽니다.\n" +
                "2. 피크플로우미터는 사용자가 내쉰 공기의 최고 속도를 측정합니다.\n" +
                "3. 이 측정치는 L/min(리터/분) 단위로 표시되며, 사용자의 호흡기 건강 상태를 평가하는 데 사용됩니다."

        binding.content3.text = "Q. 사용자가 내쉰 공기의 최고 속도를 어떻게 측정할 수 있을까?\n\n" +
                "<< 기구의 구조 >>\n" +
                "피크플로우미터는 일반적으로 플라스틱 튜브로 구성되어 있으며, 내부에는 피스톤 또는 슬라이딩 게이지가 있습니다.\n" +
                "사용자가 숨을 내쉴 때 공기의 흐름을 측정하는 스프링 장치와 눈금 표시가 있습니다.\n\n" +
                "<< 작동 원리 >>\n" +
                "사용자가 깊게 숨을 들이쉰 후 마우스피스에 입을 대고 가능한 한 빠르게, 강하게 숨을 내쉽니다.\n" +
                "숨을 내쉴 때 생성된 공기 흐름이 피크플로우미터 내부의 피스톤 또는 게이지를 밀어 움직이게 합니다.\n" +
                "이 움직임은 스프링 장치에 의해 저항을 받으며, 공기 흐름의 최대 속도에 도달했을 때 피스톤 또는 게이지가 최대 위치에서 멈춥니다.\n" +
                "피크플로우미터의 눈금 표시가 이 최대 위치를 나타내며, 이를 통해 공기 흐름의 최고 속도를 읽을 수 있습니다.\n\n" +
                "<< 물리적 원리 >>\n" +
                "피크플로우미터는뉴턴의 제2법칙(Newton's Second Law)**에 기반하여 작동합니다.\n" +
                "공기 흐름 속도가 빠를수록 튜브 내의 압력이 낮아지고, 이로 인해 피스톤이나 게이지가 더 많이 이동하게 됩니다.\n" +
                "사용자가 숨을 내쉴 때 생성되는 힘과 스프링 장치의 저항 사이의 균형을 통해 최대 공기 흐름 속도를 정확하게 측정할 수 있습니다."

        binding.content4.text = "<< 뉴턴의 제2법칙 >>\n" +
                "물체의 운동량의 시간에 따른 변화율은 그 물체에 작용하는 힘과 같다. \n\n" +
                "물체에 더 큰 알짜힘이 가해질수록 물체의 운동량의 변화는 더 커진다.\n\n" +
                "불어넣는 바람이 크면 프로펠러가 더 많이 회전한다."

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}