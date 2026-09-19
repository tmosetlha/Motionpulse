package com.the5watermelons.motionpulse.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.the5watermelons.motionpulse.R

class WeeklyLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val chartPath = Path()

    // Default zig-zag values representing the mood from the screenshot mockup
    private var dataPoints = listOf(0.6f, 0.4f, 0.7f, 0.3f, 0.6f, 0.2f, 0.8f)

    init {
        linePaint.color = ContextCompat.getColor(context, R.color.mp_blue)
        dotPaint.color = ContextCompat.getColor(context, R.color.mp_blue)
    }

    fun setDataPoints(points: List<Float>) {
        if (points.size == 7) {
            dataPoints = points
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val paddingLeft = paddingLeft + 40f
        val paddingRight = paddingRight + 40f
        val paddingTop = paddingTop + 40f
        val paddingBottom = paddingBottom + 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val stepX = chartWidth / 6f
        chartPath.reset()

        val pointsCoordinates = dataPoints.mapIndexed { index, value ->
            val x = paddingLeft + index * stepX
            // Invert Y axis since 0,0 is top-left
            val y = paddingTop + chartHeight * (1f - value)
            Pair(x, y)
        }

        // Draw line connecting the points
        pointsCoordinates.forEachIndexed { index, pair ->
            if (index == 0) {
                chartPath.moveTo(pair.first, pair.second)
            } else {
                chartPath.lineTo(pair.first, pair.second)
            }
        }
        canvas.drawPath(chartPath, linePaint)

        // Draw dots at each point
        pointsCoordinates.forEach { pair ->
            canvas.drawCircle(pair.first, pair.second, 10f, dotPaint)
        }
    }
}