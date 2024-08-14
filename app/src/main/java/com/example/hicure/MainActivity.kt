package com.example.hicure

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.util.ArrayList
import com.example.hicure.utils.FirebaseCheckDate

data class ChartData(
    var lableData: String = "",
    var lineData: Double = 0.0
)

class MainActivity : AppCompatActivity() {
    private var database: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    private val userRef: DatabaseReference = database.getReference("users")

    lateinit var lineChart: LineChart
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var Myscore: TextView
    private val chartData = ArrayList<MyChartData1>()

    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bn_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lineChart = findViewById(R.id.linechart)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val userRefValue = sharedPreferences.getInt("reference_value", 0)

        binding.reference.setText("나의 폐활량 정적치  : $userRefValue")

        val userName = sharedPreferences.getString("user_name", null)
        val userId = sharedPreferences.getString("user_id", null)

        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        val textViewDate: TextView = findViewById(R.id.textViewDate)
        textViewDate.text = currentDate
        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime
        val lungImage: ImageView = findViewById(R.id.lungimage)

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

        userName?.let {
            "$it".also { binding.username.text = it }
        }
        setupLineChart(1.toString())
        Myscore = findViewById(R.id.myscore)

        binding.leftB.visibility = View.GONE
        binding.countNumber.text = "1 회차"
        var num = 1

        binding.leftB.setOnClickListener {

            num--
            binding.countNumber.text = "$num 회차"
            setupLineChart(num.toString(), userId!!, userRefValue!!)

            if (num == 1) {
                binding.leftB.visibility = View.GONE
            }
        }

        binding.rightB.setOnClickListener {

            num++
            binding.countNumber.text = "$num 회차"
            setupLineChart(num.toString(), userId!!, userRefValue!!)

            if (binding.leftB.visibility == View.GONE) {
                binding.leftB.visibility = View.VISIBLE
            }
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

        userId?.let {
            userRef.child(it).child("score")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val score = dataSnapshot.getValue(Int::class.java) ?: 0
                        Myscore.text = score.toString()

                        setupPieChart(score.toString())
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Myscore.text = "Failed to load score."
                    }
                })
        }

        // 날짜가 변경되었는지 체크하고, 필요한 경우 점수 업데이트
        FirebaseCheckDate.updateDate(userId!!)
        checkAndSetupInitialChart(userId, userRefValue)
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userRefValue = sharedPreferences.getInt("reference_value", 0)
        val userId = sharedPreferences.getString("user_id", null)

        userId?.let {
            userRef.child(it).child("score").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val score = dataSnapshot.getValue(Double::class.java) ?: 0.0 // Double 타입으로 받음
                    Myscore.text = score.toString()

                    // 사용자 이름과 점수를 결합하여 표시
                    userName?.let {
                        val myscore = "나의 점수: ${score}"
                        binding.myscore.text = myscore

                        // 점수를 숫자형으로 비교
                        if (score >= 60) { // score를 직접 비교
                            lungImage.setImageResource(R.drawable.goodlung)
                        } else {
                            lungImage.setImageResource(R.drawable.badlung)
                        }
                    }
                })

            // maxValue 갱신
            checkAndSetupInitialChart(userId, userRefValue)
        }
    }

    private fun checkAndSetupInitialChart(userId: String, userRefValue: Int) {
        val currentDate = LocalDate.now().toString()

        if (userId != null) {
            // Firebase 데이터베이스 참조
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val dataRef: DatabaseReference =
                database.getReference("users/$userId/data/$currentDate")

            // 로딩 중에 ProgressBar를 표시하고 LineChart를 숨김
            binding.progressBar.visibility = View.VISIBLE
            lineChart.visibility = View.GONE

            // Firebase에서 데이터 가져오기
            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 로딩 완료 후, ProgressBar를 숨기고 LineChart를 표시
                    binding.progressBar.visibility = View.GONE
                    lineChart.visibility = View.VISIBLE

                    if (snapshot.exists()) {
                        // 데이터가 존재하면 첫 회차로 설정하고 차트 표시
                        binding.countNumber.text = "1 회차"
                        binding.dataChart.visibility = View.VISIBLE
                        binding.noneData.visibility = View.GONE

                        setupLineChart("1", userId, userRefValue)
                    } else {
                        // 데이터가 없으면 차트를 숨김
                        binding.dataChart.visibility = View.GONE
                        binding.noneData.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Myscore.text = "Failed to load score."
                }
            })
        }
    }

    private fun updateScoreAndResetGap(userId: String, gapValue: Int) {
        userRef.child(userId).child("score").get()
            .addOnSuccessListener { scoreSnapshot ->
                val currentScore = scoreSnapshot.getValue(Int::class.java) ?: 0
                val newScore = currentScore + gapValue

                userRef.child(userId).child("score").setValue(newScore)
                    .addOnSuccessListener {
                        Myscore.text = newScore.toString()
                        binding.gapValue.text = "0"  // gapValue 초기화
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "점수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "현재 점수를 가져오는데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun setupPieChart(sco: String) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // sco 값을 숫자로 변환 (예: "52.0" -> 52f)
        val scoreValue = sco.toFloatOrNull() ?: 0f

        // 52%와 48%로 나누기
        val values = listOf(scoreValue, 100f - scoreValue)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(values[0], "$scoreValue"))
        entries.add(PieEntry(values[1], ""))

        val dataSet = PieDataSet(entries, "Pie Chart Data")
        dataSet.colors = listOf(Color.DKGRAY, Color.LTGRAY)
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

    private fun setupLineChart(cnt: String) {
        lineChart = findViewById(R.id.linechart)
        val currentDate = LocalDate.now().toString()

        if (userId != null) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val dataRef: DatabaseReference =
                database.getReference("users/$userId/data/$currentDate")

            // 로딩 중에 ProgressBar를 표시하고 LineChart를 숨김
            binding.progressBar.visibility = View.VISIBLE
            lineChart.visibility = View.GONE

            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chartData.clear() // 기존 데이터 초기화
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val label = dataSnapshot.key?.replace("[^\\d.]".toRegex(), "") ?: "0"
                            val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                            addChartItem(label + "초", value)
                        }
                        updateChart()
                    } else {
                        updateChart()
                        Log.e("Firebase", "No data found for the given path")
                    }
                    updateChart()
                }


                override fun onCancelled(error: DatabaseError) {
                    // 로딩 실패 시에도 ProgressBar를 숨김
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            })

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
        val item = MyChartData1(labelItem, dataItem)
        chartData.add(item)
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}

data class MyChartData1(val labelData: String, val lineData: Double)