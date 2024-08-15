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

class ActivityDeviceScience : AppCompatActivity() {
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

        binding.content4.visibility = View.GONE
        binding.content5.visibility = View.GONE
        binding.image.visibility = View.GONE

        binding.content1.text = "<< 피크플로우미터의 구성 요소 >>\n\n" +
                "1. 리드 스위치\n자기장에 의해 작동되는 전자기 스위치로, 작은 유리관 내부에 두 개의 금속 리드(lead)가 위치해 있습니다. 외부 자석이 접근하면 두 리드가 접촉하여 회로를 완성합니다.\n" +
                "2. 자석\n 사용자가 숨을 불어넣을 때 자석이 이동하여 리드 스위치의 상태를 변화시킵니다.\n" +
                "3. 센서 및 전자 회로\n 리드 스위치의 상태 변화를 감지하여 전기적 신호로 변환하고, 이를 처리하여 피크 유속을 계산하는 회로입니다.\n" +
                "4. 그 외, 충전 배터리, 스위치, 압력센서 등 보정 장치가 포함되어있습니다.."

        binding.content2.text = "<< 작동 원리 >>\n\n" +
                "1. 호기 유속 생성\n 사용자가 튜브를 통해 숨을 세게 불어넣습니다. 이때 발생하는 호기 유속이 프로펠러에 부착된 자석을 움직이게 합니다.\n" +
                "2. 자석의 이동\n 자석의 위치는 호기 유속에 비례하여 회전합니다.\n" +
                "3. 리드 스위치 작동\n 자석이 리드 스위치에 접근하면, 자석의 자기장에 의해 리드 스위치가 닫히거고 멀어지면 스위치가 열립니다.\n" +
                "4. 전기 신호 생성\n 리드 스위치의 상태 변화(닫힘 또는 열림)를 전자 회로가 감지하여 전기 신호로 변환합니다.\n" +
                "5. 신호 처리\n 전자 회로가 전기 신호를 분석하여 호기 유속을 계산합니다. 이 데이터를 통해 사용자의 피크 유속을 측정합니다."

        binding.content3.text = "<< 과학적 원리 >>\n\n" +
                "- 자기장과 리드 스위치 -\n 자석이 리드 스위치에 접근하면, 자석의 자기장이 리드 스위치 내부의 두 금속 리드를 접촉시켜 회로를 닫습니다. 자석이 멀어지면 자기장이 감소하여 리드가 다시 분리됩니다.\n\n" +
                "- 유체 역학 -\n 사용자가 불어넣은 공기의 속도(호기 유속)는 프로펠러의 회전 수와 비례합니다. 프로펠러 날개의 자석의 회전 수는 호기 유속을 나타냅니다.\n\n" +
                "- 전자 회로 -\n 리드 스위치의 상태 변화를 감지하여 전기적 신호로 변환하고, 이를 처리하여 정확한 피크 유속을 계산합니다."

        binding.backBtn.setOnClickListener{
            finish()
        }
    }
}