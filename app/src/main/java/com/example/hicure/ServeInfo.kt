package com.example.hicure

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
            InfoItem("Title 1", "Content 1", false),
            InfoItem("Title 2", "Content 2", true),
            InfoItem("Title 3", "Content 3", false)
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
        // val selectedItem = adapter.items[position]
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
