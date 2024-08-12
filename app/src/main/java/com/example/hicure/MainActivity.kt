package com.example.hicure

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityMainBinding
import android.view.ViewTreeObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.Toast
import android.widget.RelativeLayout
import com.example.hicure.serveinfo.ServeInfo
import com.example.hicure.alarm.AlarmList
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.ArrayList

data class ChartData(
    var lableData: String = "",
    var lineData: Double = 0.0
)

class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    lateinit var lineChart: LineChart
    private val chartData = ArrayList<ChartData>()
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val frame: RelativeLayout by lazy { // activity_main의 화면 부분
        findViewById(R.id.main)
    }
    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bn_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        val textViewDate: TextView = findViewById(R.id.textViewDate)
        textViewDate.text = currentDate
        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime

        val button1: Button = findViewById(R.id.new_measure)
        button1.setOnClickListener {
            val intent = Intent(this, BleConnect::class.java)
            startActivity(intent)
        }
        val button2: Button = findViewById(R.id.calender)
        button2.setOnClickListener {
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        }

        bottomNagivationView.selectedItemId = R.id.ic_Home

        binding.bnMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_Home -> startNewActivity(MainActivity::class.java)
                R.id.ic_Alarm -> startNewActivity(AlarmList::class.java)
                R.id.ic_Serve -> startNewActivity(ServeInfo::class.java)
                R.id.ic_User -> startNewActivity(UserInfo::class.java)
            }
            true
        }

        bottomNagivationView.selectedItemId = R.id.ic_Home

        "오늘의 폐건강".also { binding.actionTitle.text = it }

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

        setupPieChart()
        setupLineChart()

        val userName = getUserNameFromPreferences()
        userName?.let {
            "$it".also { binding.username.text = it }
        }
    }

    // 화면 전환 구현 메소드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(frame.id, fragment).commit()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("hh : mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, ""))
        entries.add(PieEntry(60f, ""))

        val dataSet = PieDataSet(entries, "Election Results")
        dataSet.colors = listOf(Color.GRAY, Color.LTGRAY)
        dataSet.setDrawValues(false)

        // 도넛 차트 효과를 위해 가운데 원을 비웁니다.
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun setupLineChart() {
        lineChart = findViewById(R.id.linechart)

        // LineChart 데이터 초기화
        chartData.clear()
        addChartItem("1월", 7.9)
        addChartItem("2월", 8.2)
        addChartItem("3월", 8.3)
        addChartItem("4월", 8.5)
        addChartItem("5월", 7.3)

        val entries = mutableListOf<Entry>()

        for (item in chartData) {
            entries.add(
                Entry(
                    item.lableData.replace("[^\\d.]".toRegex(), "").toFloat(),
                    item.lineData.toFloat()
                )
            )
        }

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.color = Color.BLUE // LineChart에서 Line Color 설정
        lineDataSet.setCircleColor(Color.DKGRAY) // LineChart에서 Line Circle Color 설정
        lineDataSet.setCircleHoleColor(Color.DKGRAY) // LineChart에서 Line Hole Circle Color 설정

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet)

        val data = LineData(dataSets)

        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }

    private fun addChartItem(labelItem: String, dataItem: Double) {
        val item = ChartData(labelItem, dataItem)
        chartData.add(item)
    }

    private fun getUserNameFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_name", null)
    }
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}