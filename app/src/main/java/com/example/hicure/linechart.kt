package com.example.hicure


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.ArrayList

class LineChart : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    lateinit var lineChart: LineChart
    private val chartData = ArrayList<ChartData>()

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
        val item = ChartData(labelItem, dataItem)
        chartData.add(item)
    }
}