package com.example.hicure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class CustomTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val amPm = listOf("AM", "PM")
    private val hours = (1..12).map { String.format("%02d", it) }
    private val minutes = (0..59).map { String.format("%02d", it) }

    private var selectedAmPm = 0
    private var selectedHour = 0
    private var selectedMinute = 0

    private var itemHeight = 0f
    private var scrollY = 0f
    private val scrollSpeedFactor = 0.5f

    private val selectedTextSize: Float
    private val normalTextSize: Float
    private val amPmTextSize: Float

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomTimePicker,
            0, 0
        ).apply {
            try {
                selectedTextSize = getDimension(R.styleable.CustomTimePicker_selectedTextSize, 50f)
                normalTextSize = getDimension(R.styleable.CustomTimePicker_normalTextSize, 45f)
                amPmTextSize = getDimension(R.styleable.CustomTimePicker_amPmTextSize, 25f)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        itemHeight = (height / 5f)

        val amPmWidth = width * 0.25f
        val hourWidth = width * 0.25f
        val colonWidth = width * 0.005f
        val minuteWidth = width * 0.25f

        drawAmPmColumn(canvas, amPm, amPmWidth / 2, selectedAmPm)
        drawColumn(canvas, hours, amPmWidth + hourWidth / 2, selectedHour)
        drawColon(canvas, amPmWidth + hourWidth + colonWidth / 2)
        drawColumn(canvas, minutes, amPmWidth + hourWidth + minuteWidth / 2, selectedMinute)
    }

    private fun drawAmPmColumn(canvas: Canvas, items: List<String>, centerX: Float, selected: Int) {
        val y = height / 2f
        val offsetFactor = 0.02f

        for (i in items.indices) {
            paint.color = if (i == selected) ContextCompat.getColor(context, R.color.edge_blue)
            else ContextCompat.getColor(context, R.color.gray)
            paint.typeface = ResourcesCompat.getFont(context, R.font.oxygen_bold)
            paint.textSize = amPmTextSize

            val offset = if (i == 0) -itemHeight * offsetFactor else itemHeight
            canvas.drawText(items[i], centerX, y + offset, paint)
        }
    }

    private fun drawColumn(canvas: Canvas, items: List<String>, centerX: Float, selected: Int) {
        for (i in -2..2) {
            val index = (selected + i + items.size) % items.size
            val y = height / 2f + i * itemHeight - scrollY % itemHeight

            paint.color = if (i == 0) ContextCompat.getColor(context, R.color.edge_blue)
            else ContextCompat.getColor(context, R.color.gray)
            paint.typeface = ResourcesCompat.getFont(context, R.font.oxygen_light)
            paint.textSize = if (i == 0) selectedTextSize else normalTextSize

            canvas.drawText(items[index], centerX, y + itemHeight / 2, paint)
        }
    }

    private fun drawColon(canvas: Canvas, centerX: Float) {
        val y = height / 2f

        paint.color = ContextCompat.getColor(context, R.color.edge_blue)
        paint.typeface = ResourcesCompat.getFont(context, R.font.oxygen_light)
        paint.textSize = selectedTextSize

        canvas.drawText(":", centerX, y + itemHeight / 2, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val amPmWidth = width * 0.25f
                val hourWidth = width * 0.375f
                val column = when {
                    x < amPmWidth -> 0
                    x < amPmWidth + hourWidth -> 1
                    else -> 2
                }

                when (column) {
                    0 -> {
                        selectedAmPm = if (y < height / 2) 0 else 1
                    }
                    1 -> {
                        val dy = event.y - height / 2
                        scrollY += dy * scrollSpeedFactor
                        selectedHour = ((selectedHour - (scrollY / itemHeight).toInt() + hours.size) % hours.size).coerceIn(0, hours.size - 1)
                        scrollY = 0f
                    }
                    2 -> {
                        val dy = event.y - height / 2
                        scrollY += dy * scrollSpeedFactor
                        selectedMinute = ((selectedMinute - (scrollY / itemHeight).toInt() + minutes.size) % minutes.size).coerceIn(0, minutes.size - 1)
                        scrollY = 0f
                    }
                }

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                scrollY = 0f
            }
        }
        return super.onTouchEvent(event)
    }

    fun setHour(hour: Int) {
        selectedHour = (hours.indexOf(String.format("%02d", hour)).coerceIn(0, hours.size - 1))
        invalidate()
    }

    fun setMinute(minute: Int) {
        selectedMinute = (minutes.indexOf(String.format("%02d", minute)).coerceIn(0, minutes.size - 1))
        invalidate()
    }

    fun setAmPm(amPm: Int) {
        selectedAmPm = amPm.coerceIn(0, 1)
        invalidate()
    }

    fun getSelectedTime(): String {
        val hour = hours[selectedHour]
        val minute = minutes[selectedMinute]
        val period = amPm[selectedAmPm]
        return "$hour:$minute $period"
    }

    fun setSelectedTime(time: String) {
        val parts = time.split(":")
        val hourMinute = parts[0].trim()
        val minuteAmPm = parts[1].trim().split(" ")

        val hour = hourMinute.toInt()
        val minute = minuteAmPm[0].toInt()
        val amPm = minuteAmPm[1]

        setHour(hour)
        setMinute(minute)
        setAmPm(if (amPm.equals("AM", ignoreCase = true)) 0 else 1)
    }
}