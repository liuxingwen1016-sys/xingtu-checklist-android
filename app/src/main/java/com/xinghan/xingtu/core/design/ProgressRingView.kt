package com.xinghan.xingtu.core.design

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.xinghan.xingtu.R
import kotlin.math.min

/**
 * Lightweight ring progress view: a track circle, a progress arc and a
 * centered percent label. Used on the home hero card, detail header and
 * the widget-preview surfaces so every screen shares one visual language.
 */
class ProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private val arcRect = RectF()
    private val textCenterY = Paint.FontMetrics()

    private var percent: Int = 0

    private val ringStrokeWidthPx: Float
    private val density = resources.displayMetrics.density

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.ProgressRingView, defStyleAttr, 0)
        val trackColor = typed.getColor(
            R.styleable.ProgressRingView_trackColor,
            context.getColor(R.color.colorDivider),
        )
        val progressColor = typed.getColor(
            R.styleable.ProgressRingView_ringColor,
            context.getColor(R.color.colorPrimary),
        )
        val labelColor = typed.getColor(
            R.styleable.ProgressRingView_ringLabelColor,
            context.getColor(R.color.colorTextPrimary),
        )
        ringStrokeWidthPx = typed.getDimension(
            R.styleable.ProgressRingView_ringStrokeWidth,
            8f * density,
        )
        val labelSize = typed.getDimension(
            R.styleable.ProgressRingView_ringLabelSize,
            14f * density,
        )
        typed.recycle()

        trackPaint.color = trackColor
        trackPaint.strokeWidth = ringStrokeWidthPx
        trackPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.color = progressColor
        progressPaint.strokeWidth = ringStrokeWidthPx
        progressPaint.strokeCap = Paint.Cap.ROUND
        textPaint.color = labelColor
        textPaint.textSize = labelSize
        textPaint.isFakeBoldText = true
    }

    /** Sets the completion percent (clamped to 0..100) and redraws. */
    fun setPercent(value: Int) {
        val clamped = value.coerceIn(0, 100)
        if (clamped == percent) return
        percent = clamped
        contentDescription = context.getString(
            R.string.detail_progress_content_description, clamped,
        )
        invalidate()
    }

    fun setColors(trackColor: Int, progressColor: Int, labelColor: Int) {
        trackPaint.color = trackColor
        progressPaint.color = progressColor
        textPaint.color = labelColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        if (size <= 0f) return
        val cx = width / 2f
        val cy = height / 2f
        val radius = (size - ringStrokeWidthPx - 2f * density) / 2f
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)
        val sweep = 360f * percent / 100f
        if (sweep > 0f) {
            canvas.drawArc(arcRect, START_ANGLE, sweep, false, progressPaint)
        }

        val label = "$percent%"
        textPaint.getFontMetrics(textCenterY)
        val textY = cy - (textCenterY.ascent + textCenterY.descent) / 2f
        canvas.drawText(label, cx, textY, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (48f * density).toInt()
        val width = resolveSize(desired, widthMeasureSpec)
        val height = resolveSize(desired, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    companion object {
        private const val START_ANGLE = -90f
    }
}
