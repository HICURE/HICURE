package com.example.hicure

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.hicure.databinding.ActivityCalendarBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class Calendar : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    private val userRef: DatabaseReference = database.getReference("users")
    private val binding: ActivityCalendarBinding by lazy { ActivityCalendarBinding.inflate(layoutInflater) }

    private lateinit var calendarView: CalendarView
    private lateinit var diaryTextView: TextView
    private lateinit var breathTextView: TextView
    private lateinit var lineChart: LineChart
    private lateinit var progressBar: ProgressBar
    private val chartData = ArrayList<MyChartData>()
    private var selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    private var userId: String? = null
    private var currentSession = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUI()

        userId = getUserNameFromPreferences()
        if (userId != null) {
            loadDataAndInitializeUI(selectedDate, userId!!)
            calendarView.setDate(System.currentTimeMillis(), false, true)
        } else {
            breathTextView.text = "User ID를 찾을 수 없습니다"
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            diaryTextView.text = selectedDate
            currentSession = 1
            binding.countNumber.text = "$currentSession 회차"
            binding.leftB.visibility = View.GONE
            loadDataAndInitializeUI(selectedDate, userId!!)
        }
    }

    private fun initUI() {
        binding.actionTitle.text = "캘린더"
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

        binding.diaryTextView.text = selectedDate
        diaryTextView = binding.diaryTextView
        breathTextView = binding.breathTextView
        lineChart = binding.linechart
        progressBar = binding.progressBar
        calendarView = binding.calendarView

        binding.backB.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.leftB.visibility = View.GONE
        binding.countNumber.text = "$currentSession 회차"
        setupChartNavigation()
    }

    private fun setupChartNavigation() {
        binding.leftB.setOnClickListener {
            if (currentSession > 1) {
                currentSession--
                updateChart()
                if (currentSession == 1) binding.leftB.visibility = View.GONE
            }
        }

        binding.rightB.setOnClickListener {
            currentSession++
            updateChart()
            if (currentSession > 1) binding.leftB.visibility = View.VISIBLE
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("hh : mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getUserNameFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_id", null)
    }

    private fun loadDataAndInitializeUI(date: String, userIDD: String) {
        progressBar.visibility = View.VISIBLE
        binding.dataChart.visibility = View.GONE
        breathTextView.text = "데이터 로드 중..."

        val dataRef = userRef.child(userIDD).child("data").child(date)

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val maxValue = snapshot.children.flatMap { it.children }
                    .filter { it.key != "time" }
                    .mapNotNull { it.getValue(Int::class.java) }
                    .maxOrNull()

                breathTextView.text = if (maxValue != null) {
                    "하루중 최대값 : $maxValue"
                } else {
                    "데이터가 존재하지 않습니다."
                }

                setupLineChart(currentSession.toString(), date)
            }

            override fun onCancelled(error: DatabaseError) {
                breathTextView.text = "데이터를 가져오는 데 실패했습니다"
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun setupLineChart(cnt: String, date: String) {
        progressBar.visibility = View.VISIBLE

        userId?.let { id ->
            val dataRef = userRef.child(id).child("data").child(date)

            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chartData.clear()

                    val totalSessions = snapshot.childrenCount.toInt()
                    val sessionSnapshot = snapshot.child(cnt)
                    if (sessionSnapshot.exists()) {
                        sessionSnapshot.children.forEach { dataSnapshot ->
                            val key = dataSnapshot.key
                            if (key == "time") {
                                binding.textViewTime.text = dataSnapshot.getValue(String::class.java)
                            } else {
                                val label = key?.replace("[^\\d.]".toRegex(), "") ?: "0"
                                val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                                addChartItem("$label 초", value)
                            }
                        }

                        updateChart()
                        binding.dataChart.visibility = View.VISIBLE
                        binding.noneData.visibility = View.GONE
                    } else {
                        binding.dataChart.visibility = View.GONE
                        binding.noneData.visibility = View.VISIBLE
                    }

                    if (cnt.toInt() >= totalSessions) {
                        binding.rightB.visibility = View.GONE
                    } else {
                        binding.rightB.visibility = View.VISIBLE
                    }

                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    breathTextView.text = "데이터를 가져오는 데 실패했습니다"
                    progressBar.visibility = View.GONE
                    binding.dataChart.visibility = View.GONE
                    binding.noneData.visibility = View.VISIBLE
                }
            })
        } ?: run {
            breathTextView.text = "User ID를 찾을 수 없습니다"
            progressBar.visibility = View.GONE
        }
    }

    private fun updateChart() {
        binding.countNumber.text = "$currentSession 회차"
        setupLineChart(currentSession.toString(), selectedDate)
        val entries = chartData.map { Entry(it.labelData.replace("[^\\d.]".toRegex(), "").toFloat(), it.lineData.toFloat()) }

        val lineDataSet = LineDataSet(entries, "").apply {
            color = Color.BLUE
            setCircleColor(Color.DKGRAY)
            setCircleHoleColor(Color.DKGRAY)
        }

        lineChart.apply {
            data = LineData(listOf<ILineDataSet>(lineDataSet))
            description.isEnabled = false
            invalidate()
        }
    }

    private fun addChartItem(labelItem: String, dataItem: Double) {
        chartData.add(MyChartData(labelItem, dataItem))
    }
}

data class MyChartData(val labelData: String, val lineData: Double)
