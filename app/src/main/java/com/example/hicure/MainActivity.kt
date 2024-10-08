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
    var labelData: String = "",
    var lineData: Double = 0.0
)

class MainActivity : AppCompatActivity() {
    private var database: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    private val userRef: DatabaseReference = database.getReference("users")

    lateinit var lineChart: LineChart
    private val chartData = ArrayList<ChartData>()
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var Myscore: TextView

    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bn_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.ui.home.visibility = View.VISIBLE
        binding.ui.homeText.setTextColor(resources.getColor(R.color.edge_blue, null))

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
            // score 갱신
            userRef.child(it).child("score")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val score = dataSnapshot.getValue(Int::class.java) ?: 0
                        Myscore.text = score.toString()

                            // 점수를 숫자형으로 비교
                        if (score >= 60) { // score를 직접 비교
                            binding.lungimage.setImageResource(R.drawable.goodlung)
                        } else {
                            binding.lungimage.setImageResource(R.drawable.badlung)
                        }

                        setupPieChart(score.toString())
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(this@MainActivity.toString(), "Failed Load Date", databaseError.toException())
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

                override fun onCancelled(error: DatabaseError) {
                    // 로딩 실패 시에도 ProgressBar를 숨김
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
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

        val scoreValue = sco.toFloatOrNull() ?: 0f

        val values = listOf(scoreValue, 100f - scoreValue)

        val entries = ArrayList<PieEntry>()
        if (values.size >= 2) {
            entries.add(PieEntry(values[0], ""))
            entries.add(PieEntry(values[1], ""))
        } else {
            entries.add(PieEntry(100f, "")) // If not enough values, display default
        }

        val dataSet = PieDataSet(entries, "Pie Chart Data")
        dataSet.colors = listOf(Color.parseColor("#5184ED"), Color.parseColor("#E8EDF2"))
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

    private fun setupLineChart(cnt: String, userId: String, userRefValue: Int) {
        lineChart = findViewById(R.id.linechart)
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
                        val totalSessions = snapshot.childrenCount.toInt()

                        var maxValue = 0.0 // 모든 회차 중에서 최대값을 저장할 변수

                        // 모든 회차 데이터를 순회하면서 최대값 계산
                        for (sessionSnapshot in snapshot.children) {
                            for (dataSnapshot in sessionSnapshot.children) {
                                val key = dataSnapshot.key
                                if (key != "time") { // 시간 데이터는 제외하고 값만 비교
                                    val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                                    if (value > maxValue) {
                                        maxValue = value // 최대값 갱신
                                    }
                                }
                            }
                        }

                        // 현재 선택된 회차의 데이터를 표시
                        val sessionRef = snapshot.child(cnt)
                        if (sessionRef.exists()) {
                            chartData.clear()

                            for (dataSnapshot in sessionRef.children) {
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

                            binding.dataChart.visibility = View.VISIBLE
                            binding.noneData.visibility = View.GONE

                            updateChart()

                            // 최대값을 maxValue TextView에 설정
                            binding.maxValue.text = "오늘 나의 최대 수치 : $maxValue"

                            // 기준값과의 차이 계산 및 점수 할당
                            val difference = maxValue - userRefValue
                            val score = when {
                                difference < -100 -> 1
                                difference in -100.0..50.0 -> 2
                                else -> 3
                            }

                            // gapValue에 점수 표시
                            binding.gapValue.text = "+ $score"

                            userRef.child(userId).child("gapValue").setValue(score)

                            // 현재 회차가 마지막 회차라면 오른쪽 버튼을 숨김
                            if (cnt.toInt() >= totalSessions) {
                                binding.rightB.visibility = View.GONE
                            } else {
                                binding.rightB.visibility = View.VISIBLE
                            }

                        } else {
                            binding.countNumber.text = "$totalSessions 회차"
                            setupLineChart(totalSessions.toString(), userId, userRefValue)
                        }

                    } else {
                        // 오늘 날짜의 데이터가 없을 경우
                        binding.dataChart.visibility = View.GONE
                        binding.noneData.visibility = View.VISIBLE
                        binding.rightB.visibility = View.GONE
                        binding.leftB.visibility = View.GONE
                    }
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
        val item = ChartData(labelItem, dataItem)
        chartData.add(item)
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}