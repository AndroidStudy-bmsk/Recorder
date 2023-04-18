package org.bmsk.recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>()
    private val rectList = mutableListOf<RectF>()
    private val rectWidth = 13f
    private var tick = 0

    private val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for(rectF in rectList) {
            canvas?.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {
        val amplitude = maxAmplitude / Short.MAX_VALUE * this.height * 0.8f // 0 ~ 1 값으로 만들고 전체 높이를 곱해줌

        ampList.add(amplitude)
        rectList.clear()

        val maxRect = (this.width / rectWidth).toInt()

        val amps = ampList.takeLast(maxRect)

        for((index, amp) in amps.withIndex()) {
            val rectF = RectF().apply {
                top = (this@WaveFormView.height / 2) - amp / 2
                bottom = top + amp
                left = index * rectWidth
                right = left + (rectWidth - 5f)
            }
            rectList.add(rectF)
        }

        invalidate()
    }

    fun replayAmplitude() {
        rectList.clear()

        val maxRect = (this.width / rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxRect)

        for((index, amp) in amps.withIndex()) {
            val rectF = RectF().apply {
                top = (this@WaveFormView.height / 2) - amp / 2
                bottom = top + amp
                left = index * rectWidth
                right = left + (rectWidth - 5f)
            }
            rectList.add(rectF)
        }

        tick++

        invalidate()
    }

    fun clearData() {
        ampList.clear()
    }

    fun clearWave() {
        rectList.clear()
        tick = 0
        invalidate()
    }
}