package com.example.hicure

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.annotation.SuppressLint
import android.graphics.Color
import java.io.FileInputStream
import java.io.FileOutputStream

import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class Calendar : AppCompatActivity() {
    val database = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    val userRef = database.getReference("users")

    var userID: String = "userID"
    lateinit var fname: String
    lateinit var str: String
    lateinit var calendarView: CalendarView
    //lateinit var updateBtn: Button
    //lateinit var deleteBtn:Button
    //lateinit var saveBtn:Button
    lateinit var diaryTextView: TextView
    lateinit var diaryContent:TextView
    lateinit var breathTextView:TextView
    lateinit var title:TextView
    //lateinit var contextEditText: EditText

    lateinit var lineChart: LineChart
    private val chartData = ArrayList<ChartData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // UI값 생성
        calendarView=findViewById(R.id.calendarView)
        diaryTextView=findViewById(R.id.diaryTextView)
        breathTextView=findViewById(R.id.breathTextView)
        //saveBtn=findViewById(R.id.saveBtn)
        //deleteBtn=findViewById(R.id.deleteBtn)
        //updateBtn=findViewById(R.id.updateBtn)
        //diaryContent=findViewById(R.id.diaryContent)
        //contextEditText=findViewById(R.id.contextEditText)



        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            diaryTextView.visibility = View.VISIBLE
            //saveBtn.visibility = View.VISIBLE
            //contextEditText.visibility = View.VISIBLE
            //diaryContent.visibility = View.INVISIBLE
            //updateBtn.visibility = View.INVISIBLE
            //deleteBtn.visibility = View.INVISIBLE
            diaryTextView.text = String.format("%d / %d / %d", year, month + 1, dayOfMonth)
            //breathTextView.text = View.VISIBLE
            //contextEditText.setText("")
            checkDay(year, month, dayOfMonth, userID)
        }
        readFirebaseData()

        /*saveBtn.setOnClickListener {
            saveDiary(fname)
            contextEditText.visibility = View.INVISIBLE
            saveBtn.visibility = View.INVISIBLE
            updateBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            str = contextEditText.text.toString()
            diaryContent.text = str
            diaryContent.visibility = View.VISIBLE
        }*/
    }

    // 달력 내용 조회, 수정
    fun checkDay(cYear: Int, cMonth: Int, cDay: Int, userID: String) {
        //저장할 파일 이름설정
        fname = "" + userID + cYear + "-" + (cMonth + 1) + "" + "-" + cDay + ".txt"

        var fileInputStream: FileInputStream
        try {
            fileInputStream = openFileInput(fname)
            val fileData = ByteArray(fileInputStream.available())
            fileInputStream.read(fileData)
            fileInputStream.close()
            str = String(fileData)
            //contextEditText.visibility = View.INVISIBLE
            //diaryContent.visibility = View.VISIBLE
            //diaryContent.text = str
            /*saveBtn.visibility = View.INVISIBLE
            updateBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            updateBtn.setOnClickListener {
                contextEditText.visibility = View.VISIBLE
                diaryContent.visibility = View.INVISIBLE
                contextEditText.setText(str)
                saveBtn.visibility = View.VISIBLE
                updateBtn.visibility = View.INVISIBLE
                deleteBtn.visibility = View.INVISIBLE
                diaryContent.text = contextEditText.text
            }
            deleteBtn.setOnClickListener {
                diaryContent.visibility = View.INVISIBLE
                updateBtn.visibility = View.INVISIBLE
                deleteBtn.visibility = View.INVISIBLE
                contextEditText.setText("")
                contextEditText.visibility = View.VISIBLE
                saveBtn.visibility = View.VISIBLE
                removeDiary(fname)
            }
            if (diaryContent.text == null) {
                diaryContent.visibility = View.INVISIBLE
                //updateBtn.visibility = View.INVISIBLE
                //deleteBtn.visibility = View.INVISIBLE
                diaryTextView.visibility = View.VISIBLE
                //saveBtn.visibility = View.VISIBLE
                //contextEditText.visibility = View.VISIBLE
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // 달력 내용 제거
    /*@SuppressLint("WrongConstant")
    fun removeDiary(readDay: String?) {
        var fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS)
            val content = ""
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }*/


    // 달력 내용 추가
    /*@SuppressLint("WrongConstant")
    fun saveDiary(readDay: String?) {
        var fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS)
            val content = contextEditText.text.toString()
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }*/
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
            entries.add(Entry(item.lableData.replace("[^\\d.]".toRegex(), "").toFloat(), item.lineData.toFloat()))
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
    private fun readFirebaseData() {
        val dataRef = userRef.child(userID).child("data").child("date").child("1")

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val yourDataValue = snapshot.getValue(String::class.java)
                    // 데이터 처리 로직을 여기에 추가하세요
                    diaryContent.text = yourDataValue
                    diaryContent.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽기 실패 처리
                error.toException().printStackTrace()
            }
        })
    }
}