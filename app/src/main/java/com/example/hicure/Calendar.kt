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
import java.time.LocalDate
import java.util.ArrayList

class Calendar : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance("https://hicure-d5c99-default-rtdb.firebaseio.com/")
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

        val backB: Button = findViewById(R.id.backB)
        backB.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // UI 초기화
        calendarView = findViewById(R.id.calendarView)
        diaryTextView = findViewById(R.id.diaryTextView)
        breathTextView = findViewById(R.id.breathTextView)

        val userId = getUserNameFromPreferences()
        val currentDate = LocalDate.now().toString()

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


        // 예시 데이터 초기화 및 설정
        setupLineChart()
    }
    private fun getUserNameFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_id", null)
    }
    // Firebase에서 데이터 읽기
    private fun readFirebaseData(date: String,userIDD: String) {
        val dataRef = userRef.child(userIDD).child("data").child(date)

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var sum=0
                val valuesList = mutableListOf<String>()

                for (sectionSnapshot in snapshot.children) {
                    // 각 섹션의 하위 항목들을 순회하며 값 합산
                    for (childSnapshot in sectionSnapshot.children) {
                        val value = childSnapshot.getValue(Int::class.java) ?: 0
                        sum += value
                        valuesList.add(value.toString())  // 각 value를 리스트에 추가
                    }
                }
                sum=sum/20
                breathTextView.text = "하루 평균 : $sum"
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 시 처리
                breathTextView.text = "데이터를 다시 생성해주세요"
            }
        })
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

