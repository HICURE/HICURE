package com.example.hicure

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
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

    lateinit var calendarView: CalendarView
    lateinit var diaryTextView: TextView
    lateinit var breathTextView: TextView
    lateinit var title: TextView

    lateinit var lineChart: LineChart
    private val chartData = ArrayList<MyChartData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        val textViewDate: TextView = findViewById(R.id.diaryTextView)
        textViewDate.text = currentDate
        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime

        val backB: Button = findViewById(R.id.backB)
        backB.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val min=findViewById<Button>(R.id.leftB)
        val plus =findViewById<Button>(R.id.rightB)
        val count_number =findViewById<TextView>(R.id.count_number)
        var num=1

        setupLineChart(1.toString())
        min.setOnClickListener{
            num--
            count_number.setText(num.toString())
            // 예시 데이터 초기화 및 설정
            setupLineChart(num.toString())
        }
        plus.setOnClickListener{
            num++
            count_number.setText(num.toString())
            // 예시 데이터 초기화 및 설정
            setupLineChart(num.toString())
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
            val date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            diaryTextView.visibility = View.VISIBLE
            diaryTextView.text = date

            if (userId != null) {
                readFirebaseData(date, userId)
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
                if(maxValue<0){
                    maxValue=0
                }
                breathTextView.text = "하루중 최대값 : $maxValue"
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 시 처리
                breathTextView.text = "데이터를 다시 생성해주세요"
            }
        })
    }


    private fun setupLineChart(cnt:String) {
        lineChart = findViewById(R.id.linechart)
        val userId = getUserNameFromPreferences()
        val currentDate = LocalDate.now().toString()

        if (userId != null) {
            // Firebase 데이터베이스 참조
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val dataRef: DatabaseReference = database.getReference("users/$userId/data/$currentDate/$cnt")

            // Firebase에서 데이터 가져오기
            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chartData.clear() // 기존 데이터 초기화

                    // Firebase 데이터 가져오기
                    for (dataSnapshot in snapshot.children) {
                        val label = dataSnapshot.key?.replace("[^\\d.]".toRegex(), "") ?: "0"
                        val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                        addChartItem(label + "초", value)
                    }

                    // 차트 업데이트
                    updateChart()
                }

                override fun onCancelled(error: DatabaseError) {
                    breathTextView.text = "데이터를 가져오는 데 실패했습니다: ${error.message}"
                }
            })
        } else {
            breathTextView.text = "User ID를 찾을 수 없습니다"
        }
    }

    private fun updateChart() {
        // 차트 데이터를 추가한 후, 차트를 업데이트하는 함수
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
