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

class ActivityHealthyFoods : AppCompatActivity() {
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

        binding.content1.text = "<< 1. 항산화제 풍부한 음식 >>\n\n" +
                "예: 블루베리, 딸기, 라즈베리 등\n\n" +
                "- 과학적 원리 - \n항산화제는 활성산소를 중화시켜 세포 손상을 예방합니다. 폐는 공기 중의 오염물질과 독소에 많이 노출되기 때문에, 항산화제는 폐 세포를 보호하고 염증을 줄이는 데 도움이 됩니다. 이를 통해 폐 기능이 향상됩니다 .\n\n" +
                "Q. 활성산소란?\n" +
                "체내에서 생성되는 불안정한 산소이다. 반응성이 커서 세포의 노화를 촉진시킨다."
        binding.content2.text = "<< 2. 오메가-3 지방산 >>\n\n" +
                "예: 연어, 고등어, 참치 등\n\n" +
                "- 과학적 원리 - \n오메가-3 지방산은 항염증 특성을 가지고 있어 폐의 염증 반응을 줄여줍니다. \n" +
                "천식과 같은 만성 호흡기 질환의 경우 염증이 중요한 역할을 하므로, 오메가-3 지방산을 섭취하면 호흡기 건강이 개선될 수 있습니다 ."
        binding.content3.text = "<< 3. 비타민 C가 풍부한 음식 >>\n\n" +
                "예: 오렌지, 레몬, 키위, 피망 등\n\n" +
                "- 과학적 원리 -\n비타민 C는 강력한 항산화제로, 폐를 포함한 신체 전반의 염증을 줄이고 면역 체계를 강화합니다. 또한, 비타민 C는 콜라겐 생성을 촉진해 폐 조직의 건강을 유지하는 데 도움이 됩니다 ."
        binding.content4.text = "<< 4. 비타민 E >>\n\n" +
                "예: 아몬드, 해바라기씨, 시금치 등\n\n" +
                "- 과학적 원리 -\n비타민 E도 항산화제 역할을 하여 폐 세포를 활성산소로부터 보호합니다. 특히 노화 과정에서 폐 기능이 저하되는 것을 늦추는 데 도움이 됩니다 ."
        binding.content5.text = "<< 5. 섬유질 >>\n\n" +
                "예: 통곡물, 콩류, 채소 등\n\n" +
                "- 과학적 원리 -\n섬유질이 풍부한 음식은 장 건강을 촉진하고 염증을 줄이는 데 도움이 됩니다.\n" +
                "장 건강과 폐 건강은 밀접한 관련이 있으며, 장내 미생물군의 균형이 폐의 면역 반응과 염증 수준에 영향을 미친다는 연구 결과가 있습니다 ."

        binding.backBtn.setOnClickListener{
            finish()
        }
    }
}