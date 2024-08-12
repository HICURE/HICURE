package com.example.hicure.serveinfo

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hicure.AlarmList
import com.example.hicure.MainActivity
import com.example.hicure.R
import com.example.hicure.UserInfo
import com.example.hicure.databinding.ActivityServeInfoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ServeInfo : AppCompatActivity(), InfoAdapter.OnItemClickListener {

    private lateinit var adapter: InfoAdapter
    private val binding: ActivityServeInfoBinding by lazy { ActivityServeInfoBinding.inflate(layoutInflater) }
    private val bottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.bn_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bottomNavigationView.selectedItemId = R.id.ic_Serve

        binding.bnMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_Home -> startNewActivity(MainActivity::class.java)
                R.id.ic_Alarm -> startNewActivity(AlarmList::class.java)
                R.id.ic_Serve -> startNewActivity(ServeInfo::class.java)
                R.id.ic_User -> startNewActivity(UserInfo::class.java)
            }
            true
        }

        // Example list of InfoItem objects
        val infoItems = listOf(
            InfoItem("‘폐’ 해부생리학적 정보", "폐의 구조와 기능에 대해 알아보기", false),
            InfoItem("폐활량을 측정하는 과학적인 원리", "폐활량 측정 방법과 기기의 원리", false),
            InfoItem("개발된 기기에 사용된 과학 원리", "기기 작동에 사용된 과학적 원리 탐구", false),
            InfoItem("폐 건강을 지켜주는 유익한 음식들", "폐활량에 도움이 되는 음식", false),
            InfoItem("폐활량 향상에 효과적인 운동 방법", "폐활량을 높이는 운동", false),
            InfoItem("폐활량이 중요한 이유", "폐활량이 건강에 미치는 영향", false),
            InfoItem("나의 폐활량 적정치 알기", "연령, 나이, 성별에 따른 폐활량 예측치", false)
        )

        // Set up the RecyclerView
        binding.infoList.layoutManager = LinearLayoutManager(this)
        adapter = InfoAdapter(this, infoItems, this)
        binding.infoList.adapter = adapter

        "폐활량 꿀팁".also { binding.actionTitle.text = it }

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
    }

    override fun onItemClick(position: Int) {
        val selectedItem = adapter.items[position] // 선택된 InfoItem 가져오기
        val intent = Intent()

        when (position) {
            0 -> intent.setClass(this, ActivityAnatomy::class.java)
            1 -> intent.setClass(this, ActivityMeasurementPrinciple::class.java)
            2 -> intent.setClass(this, ActivityDeviceScience::class.java)
            3 -> intent.setClass(this, ActivityHealthyFoods::class.java)
            4 -> intent.setClass(this, ActivityBreathExercises::class.java)
            5 -> intent.setClass(this, ActivityImportanceOfLungCapacity::class.java)
            6 -> intent.setClass(this, ActivityLungCapacityPrediction::class.java)
            else -> intent.setClass(this, MainActivity::class.java)
        }

        // Title을 Intent에 추가하여 다음 Activity에 전달
        intent.putExtra("title", selectedItem.title)
        intent.putExtra("subTitle",selectedItem.content)
        startActivity(intent)
    }


    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
