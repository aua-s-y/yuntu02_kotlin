package com.example.yuntushaomiaojia.ui.tool.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class CompassDialView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var degree = 0f

    fun setDegree(degree: Float) {
        this.degree = degree
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width - dpLocal(40f), height - dpLocal(24f)) / 2f
        if (radius <= 0f) {
            return
        }

        drawBackground(canvas, cx, cy, radius)
        drawTicks(canvas, cx, cy, radius)
        drawDirectionText(canvas, cx, cy, radius)
        drawNeedle(canvas, cx, cy, radius)
        drawCenter(canvas, cx, cy)
    }

    private fun drawBackground(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx,
            cy - radius,
            cx,
            cy + radius,
            Color.rgb(255, 244, 217),
            Color.rgb(255, 219, 196),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpLocal(4f).toFloat()
        paint.color = Color.rgb(255, 167, 66)
        canvas.drawCircle(cx, cy, radius - dpLocal(2f), paint)

        paint.strokeWidth = dpLocal(1f).toFloat()
        paint.color = Color.argb(130, 255, 186, 101)
        canvas.drawCircle(cx, cy, radius * 0.72f, paint)
        canvas.drawCircle(cx, cy, radius * 0.46f, paint)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        for (index in 0 until TICK_COUNT) {
            val major = index % 5 == 0
            val angle = Math.toRadians((index * 6 - 90).toDouble())
            val outerX = cx + cos(angle).toFloat() * (radius - dpLocal(12f))
            val outerY = cy + sin(angle).toFloat() * (radius - dpLocal(12f))
            val inner = radius - if (major) dpLocal(28f) else dpLocal(20f)
            val innerX = cx + cos(angle).toFloat() * inner
            val innerY = cy + sin(angle).toFloat() * inner
            paint.strokeWidth = if (major) dpLocal(3f).toFloat() else dpLocal(1f).toFloat()
            paint.color = if (major) Color.rgb(79, 79, 85) else Color.argb(150, 109, 109, 109)
            canvas.drawLine(innerX, innerY, outerX, outerY, paint)
        }
    }

    private fun drawDirectionText(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val labels = arrayOf("北", "东", "南", "西")
        val angles = intArrayOf(270, 0, 90, 180)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = dpLocal(20f).toFloat()
        labels.forEachIndexed { index, label ->
            val angle = Math.toRadians(angles[index].toDouble())
            val x = cx + cos(angle).toFloat() * (radius - dpLocal(52f))
            val y = cy + sin(angle).toFloat() * (radius - dpLocal(52f)) + dpLocal(7f)
            paint.color = if (index == 0) Color.rgb(255, 96, 55) else Color.rgb(25, 25, 25)
            canvas.drawText(label, x, y, paint)
        }
    }

    private fun drawNeedle(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.save()
        canvas.rotate(degree, cx, cy)

        val northNeedle = Path()
        northNeedle.moveTo(cx, cy - radius + dpLocal(64f))
        northNeedle.lineTo(cx - dpLocal(15f), cy + dpLocal(18f))
        northNeedle.lineTo(cx + dpLocal(15f), cy + dpLocal(18f))
        northNeedle.close()
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 96, 55)
        canvas.drawPath(northNeedle, paint)

        val southNeedle = Path()
        southNeedle.moveTo(cx, cy + radius - dpLocal(64f))
        southNeedle.lineTo(cx - dpLocal(11f), cy - dpLocal(10f))
        southNeedle.lineTo(cx + dpLocal(11f), cy - dpLocal(10f))
        southNeedle.close()
        paint.color = Color.rgb(79, 79, 85)
        canvas.drawPath(southNeedle, paint)

        canvas.restore()
    }

    private fun drawCenter(canvas: Canvas, cx: Float, cy: Float) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, dpLocal(18f).toFloat(), paint)
        paint.color = Color.rgb(255, 167, 66)
        canvas.drawCircle(cx, cy, dpLocal(10f).toFloat(), paint)
    }

    private fun dpLocal(value: Float): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    private companion object {
        private const val TICK_COUNT = 60
    }
}
