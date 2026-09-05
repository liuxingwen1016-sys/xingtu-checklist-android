package com.xinghan.xingtu.core.design

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.CategoryStat
import kotlin.math.min

/** Compact multi-segment category donut used by the training statistics page. */
class CategoryDonutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val arc = RectF()
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f * density
    }
    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f * density
        strokeCap = Paint.Cap.BUTT
    }
    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 20f * density
        isFakeBoldText = true
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 10f * density
    }
    private var segments: List<CategoryStat> = emptyList()
    private var percent: Int = 0

    fun setData(value: List<CategoryStat>, completionPercent: Int) {
        segments = value
        percent = completionPercent.coerceIn(0, 100)
        contentDescription = context.getString(R.string.detail_progress_content_description, percent)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (size - trackPaint.strokeWidth - 4f * density) / 2f
        arc.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        trackPaint.color = ContextCompat.getColor(context, R.color.colorDivider)
        canvas.drawArc(arc, -90f, 360f, false, trackPaint)

        val total = segments.sumOf { it.total }.coerceAtLeast(1)
        val colors = intArrayOf(
            R.color.tripThemeIndigo,
            R.color.tripThemeTeal,
            R.color.tripThemeAmber,
            R.color.tripThemeCoral,
            R.color.tripThemeViolet,
        )
        var start = -90f
        segments.forEachIndexed { index, stat ->
            if (stat.total == 0) return@forEachIndexed
            val sweep = 360f * stat.total / total
            segmentPaint.color = ContextCompat.getColor(context, colors[index % colors.size])
            canvas.drawArc(arc, start, sweep - 2f, false, segmentPaint)
            start += sweep
        }

        percentPaint.color = ContextCompat.getColor(context, R.color.colorTextPrimary)
        labelPaint.color = ContextCompat.getColor(context, R.color.colorTextSecondary)
        canvas.drawText("$percent%", centerX, centerY - 2f * density, percentPaint)
        canvas.drawText(context.getString(R.string.stats_completion_label), centerX, centerY + 16f * density, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (116f * density).toInt()
        val width = resolveSize(desired, widthMeasureSpec)
        val height = resolveSize(desired, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }
}
