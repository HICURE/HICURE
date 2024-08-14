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
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class Calendar : AppCompatActivity() {
    private val database =
        FirebaseDatabase.getInstance("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    private val userRef: DatabaseReference = database.getReference("users")

    val binding: ActivityCalendarBinding by lazy { ActivityCalendarBinding.inflate(layoutInflater) }

    lateinit var calendarView: CalendarView
    lateinit var diaryTextView: TextView
    lateinit var breathTextView: TextView
    lateinit var lineChart: LineChart
    lateinit var progressBar: ProgressBar

    private val chartData = ArrayList<MyChartData>()
    private var selectedDate: String = LocalDate.now().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        "캘린더".also { binding.actionTitle.text = it }

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

        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime

        lineChart = findViewById(R.id.linechart)
        progressBar = findViewById(R.id.progressBar)

        val backB: Button = findViewById(R.id.backB)
        backB.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.leftB.visibility = View.GONE
        binding.countNumber.text = "1 회차"
        var num = 1

        setupLineChart("1", selectedDate)

        binding.leftB.setOnClickListener {

            num--
            binding.countNumber.text = "$num 회차"
            setupLineChart(num.toString(), selectedDate)

            if (num == 1) {
                binding.leftB.visibility = View.GONE
            }
        }

        binding.rightB.setOnClickListener {

            num++
            binding.countNumber.text = "$num 회차"
            setupLineChart(num.toString(), selectedDate)

            if (binding.leftB.visibility == View.GONE) {
                binding.leftB.visibility = View.VISIBLE
            }
        }

        // UI 초기화
        calendarView = findViewById(R.id.calendarView)
        diaryTextView = findViewById(R.id.diaryTextView)
        breathTextView = findViewById(R.id.breathTextView)

        val userId = getUserNameFromPreferences()

        if (userId != null) {
            diaryTextView.visibility = View.VISIBLE
            diaryTextView.text = currentDate
            readFirebaseData(currentDate, userId)
        } else {
            breathTextView.text = "User ID를 찾을 수 없습니다"
        }

        // 날짜가 선택되었을 때 Firebase에서 데이터를 읽어옴
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            diaryTextView.visibility = View.VISIBLE
            diaryTextView.text = selectedDate
            num = 1
            binding.countNumber.text = "1 회차"
            binding.leftB.visibility = View.GONE

            if (userId != null) {
                setupLineChart("1", selectedDate) // 새로운 날짜에 대해 첫 회차 데이터를 설정
            } else {
                breathTextView.text = "User ID를 찾을 수 없습니다"
            }
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

    // Firebase에서 데이터 읽기
    private fun readFirebaseData(date: String, userIDD: String) {
        val dataRef = userRef.child(userIDD).child("data").child(date)

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var maxValue = Int.MIN_VALUE
                val valuesList = mutableListOf<String>()

                for (sectionSnapshot in snapshot.children) {
                    // 각 섹션의 하위 항목들을 순회하며 최대값을 찾음
                    for (childSnapshot in sectionSnapshot.children) {
                        val value = childSnapshot.getValue(Int::class.java) ?: 0
                        if (value > maxValue) {
                            maxValue = value
                        }
                        valuesList.add(value.toString())  // 각 value를 리스트에 추가
                    }
                }
                breathTextView.text = "하루중 최대값 : $maxValue"
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 시 처리
                breathTextView.text = "데이터를 다시 생성해주세요"
            }
        })
    }


    private fun setupLineChart(cnt: String, date: String) {
        lineChart = findViewById(R.id.linechart)
        progressBar.visibility = View.VISIBLE
        lineChart.visibility = View.GONE

        val userId = getUserNameFromPreferences()

        if (userId != null) {
            val dataRef: DatabaseReference = database.getReference("users/$userId/data/$date/$cnt")

            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        chartData.clear() // 기존 데이터 초기화

                        for (dataSnapshot in snapshot.children) {
                            val key = dataSnapshot.key
                            if (key == "time") {
                                val timeValue = dataSnapshot.getValue(String::class.java) ?: ""
                                binding.textViewTime.setText(timeValue)
                            } else {
                                val label = key?.replace("[^\\d.]".toRegex(), "") ?: "0"
                                val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                                addChartItem(label + "초", value)
                            }
                        }

                        updateChart()

                        // 데이터가 있을 경우 LineChart를 표시
                        binding.dataChart.visibility = View.VISIBLE
                        binding.noneData.visibility = View.GONE
                    } else {
                        // 데이터가 없을 경우 LineChart를 숨김
                        binding.dataChart.visibility = View.GONE
                        binding.noneData.visibility = View.VISIBLE
                        binding.breathTextView.setText("데이터가 존재하지 않습니다.")
                    }

                    lineChart.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    breathTextView.text = "데이터를 가져오는 데 실패했습니다: ${error.message}"
                    // 로딩 실패 시에도 ProgressBar를 숨김
                    progressBar.visibility = View.GONE
                    // 실패 시 차트도 숨김
                    binding.dataChart.visibility = View.GONE

                    binding.noneData.visibility = View.VISIBLE

                }
            })
        } else {
            breathTextView.text = "User ID를 찾을 수 없습니다"
            progressBar.visibility = View.GONE
        }
    }

    private fun updateChart() {
        val entries = mutableListOf<Entry>()

        for (item in chartData) {
            entries.add(
                Entry(
                    item.labelData.replace("[^\\d.]".toRegex(), "").toFloat(),
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
        val item = MyChartData(labelItem, dataItem)
        chartData.add(item)
    }
}

data class MyChartData(val labelData: String, val lineData: Double)
