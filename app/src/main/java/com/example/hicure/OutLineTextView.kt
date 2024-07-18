package com.example.hicure

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.example.hicure.R

class OutLineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int
    private var strokeWidthVal: Float

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OutLineTextView)
        strokeWidthVal = typedArray.getFloat(R.styleable.OutLineTextView_textStrokeWidth, 3f)
        strokeColor = typedArray.getColor(R.styleable.OutLineTextView_textStrokeColor, Color.WHITE)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val states: ColorStateList = textColors

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidthVal
        setTextColor(strokeColor)
        super.onDraw(canvas)

        paint.style = Paint.Style.FILL
        setTextColor(states)

        super.onDraw(canvas)
    }
}