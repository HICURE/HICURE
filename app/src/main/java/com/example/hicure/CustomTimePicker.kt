package com.example.hicure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes

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

    private fun drawAmPmColumn(canvas: Canvas, items: List<String>, centerX: Float, selected: Int) {
        val y = height / 2f

        for (i in 0..1) {
            paint.color = if (i == selected) ContextCompat.getColor(context, R.color.edge_blue)
            else ContextCompat.getColor(context, R.color.gray)
            paint.typeface = ResourcesCompat.getFont(context, R.font.oxygen_bold)
            paint.textSize = amPmTextSize

            val offset = if (i == 0) -itemHeight else itemHeight
            canvas.drawText(items[i], centerX, y + offset, paint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        itemHeight = (height / 5f)

        val amPmWidth = width * 0.25f
        val hourWidth = width * 0.25f
        val minuteWidth = width * 0.25f

        drawAmPmColumn(canvas, amPm, amPmWidth / 2, selectedAmPm)
        drawColumn(canvas, hours, amPmWidth + hourWidth / 2, selectedHour)
        drawColumn(canvas, minutes, amPmWidth + hourWidth + minuteWidth / 2, selectedMinute)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val amPmWidth = width * 0.25f
                val hourWidth = width * 0.375f
                val column = when {
                    event.x < amPmWidth -> 0
                    event.x < amPmWidth + hourWidth -> 1
                    else -> 2
                }
                when (column) {
                    0 -> {
                        selectedAmPm = if (event.y < height / 2) 0 else 1
                    }
                    1 -> {
                        scrollY += event.y * scrollSpeedFactor
                        selectedHour = ((selectedHour - (scrollY / itemHeight).toInt()) + hours.size) % hours.size
                    }
                    2 -> {
                        scrollY += event.y * scrollSpeedFactor
                        selectedMinute = ((selectedMinute - (scrollY / itemHeight).toInt()) + minutes.size) % minutes.size
                    }
                }
                scrollY = 0f
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun getSelectedTime(): String {
        val hour = hours[selectedHour]
        val minute = minutes[selectedMinute]
        val period = amPm[selectedAmPm]
        return "$period $hour:$minute"
    }
}