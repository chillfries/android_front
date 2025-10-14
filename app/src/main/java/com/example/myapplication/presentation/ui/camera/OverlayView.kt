package com.example.myapplication.presentation.ui.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import kotlin.math.max

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<DetectionResult> = emptyList()
    private val boxPaint = Paint()
    private val textPaint = Paint()
    private var scaleFactor: Float = 1.0f

    init {
        // 페인트 초기 설정
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context, R.color.purple_500)
        boxPaint.strokeWidth = 8f
        boxPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (result in results) {
            val boundingBox = result.boundingBox
            // 화면 크기에 맞게 좌표를 조정합니다.
            val left = boundingBox.left * scaleFactor
            val top = boundingBox.top * scaleFactor
            val right = boundingBox.right * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor

            // 사각형과 텍스트를 그립니다.
            canvas.drawRect(left, top, right, bottom, boxPaint)
            canvas.drawText(result.text, left, top - 10, textPaint)
        }
    }

    // 탐지 결과를 받아 화면을 다시 그리도록 요청합니다.
    fun setResults(detectionResults: List<DetectionResult>, imageWidth: Int, imageHeight: Int) {
        results = detectionResults
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate()
    }

    fun clear() {
        results = emptyList()
        invalidate()
    }
}