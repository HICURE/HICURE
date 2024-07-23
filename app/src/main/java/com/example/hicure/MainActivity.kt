package com.example.hicure

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityMainBinding
import android.view.ViewTreeObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class MainActivity : AppCompatActivity() {

    val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate((layoutInflater)) }
    private lateinit var pieChart: PieChart

    private fun setBarChart() {
        binding.pieChart.setUsePercentValues(true)

        // data set
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(14f, "Apple"))
        entries.add(PieEntry(22f, "Orange"))
        entries.add(PieEntry(7f, "Mango"))
        entries.add(PieEntry(31f, "RedOrange"))
        entries.add(PieEntry(26f, "Other"))


        // add a lot of colors
        val colorsItems = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colorsItems.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colorsItems.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colorsItems.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colorsItems.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colorsItems.add(c)
        colorsItems.add(ColorTemplate.getHoloBlue())

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            colors = colorsItems
            valueTextColor = Color.BLACK
            valueTextSize = 18f

        }

        val pieData = PieData(pieDataSet)
        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            isRotationEnabled = false
            setEntryLabelColor(Color.BLACK)
            setCenterTextSize(20f)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentDate = getCurrentDate()
        val currentTime= getCurrentTime()

        val textViewDate: TextView = findViewById(R.id.textViewDate)
        textViewDate.text = currentDate
        val textViewTime: TextView = findViewById(R.id.textViewTime)
        textViewTime.text = currentTime

        "원하는 타이틀 입력".also { binding.actionTitle.text = it }



        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

               override fun onGlobalLayout(){
                    binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                   val actionTextWidth = binding.actionTitle.width

                   binding.actionTitle.width = actionTextWidth + 10

                   // binding.mainText.text = "$actionTextWidth"

                   val layoutParams = binding.behindTitle.layoutParams
                   layoutParams.width = actionTextWidth + 30
                   binding.behindTitle.layoutParams = layoutParams

               }
        })

    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return dateFormat.format(Date())
    }
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("hh : mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}