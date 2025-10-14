package com.example.myapplication.presentation.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class ObjectDetectorHelper(
    private val context: Context,
    private val listener: DetectorListener,
    private val modelName: String = "model.tflite",
    private val labelsName: String = "labels.txt",
    private var scoreThreshold: Float = 0.6f, // 재료 인식 신뢰도 임계값
) {

    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()

    init {
        setupDetector()
    }

    private fun setupDetector() {
        try {
            val model = FileUtil.loadMappedFile(context, modelName)
            // ttemp의 성능 관련 로직(스레드 수)을 그대로 유지합니다.
            val options = Interpreter.Options().apply {
                numThreads = 4
            }
            interpreter = Interpreter(model, options)
            labels.addAll(FileUtil.loadLabels(context, labelsName))
        } catch (e: IOException) {
            listener.onError("모델 초기화에 실패했습니다: ${e.message}")
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (interpreter == null) return

        val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))
        val inputBuffer = tensorImage.buffer

        // 모델의 출력 형식에 맞게 버퍼를 준비합니다.
        // 이 값들은 사용하는 TFLite 모델에 따라 달라질 수 있습니다.
        val outputLocations = Array(1) { Array(10) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(10) }
        val outputScores = Array(1) { FloatArray(10) }
        val numDetections = FloatArray(1)

        val outputs = mapOf(
            0 to outputLocations,
            1 to outputClasses,
            2 to outputScores,
            3 to numDetections
        )

        interpreter?.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        val results = processOutput(
            outputScores[0],
            outputClasses[0],
            outputLocations[0],
            image.width,
            image.height
        )
        listener.onResults(results)
    }

    private fun processOutput(scores: FloatArray, classes: FloatArray, locations: Array<FloatArray>, imageWidth: Int, imageHeight: Int): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        for (i in scores.indices) {
            if (scores[i] >= scoreThreshold) {
                val classIndex = classes[i].toInt()
                val label = labels.getOrElse(classIndex) { "Unknown" }

                val yMin = locations[i][0] * imageHeight
                val xMin = locations[i][1] * imageWidth
                val yMax = locations[i][2] * imageHeight
                val xMax = locations[i][3] * imageWidth

                val boundingBox = RectF(
                    max(0f, xMin),
                    max(0f, yMin),
                    min(imageWidth.toFloat(), xMax),
                    min(imageHeight.toFloat(), yMax)
                )
                val text = "$label, ${"%.2f".format(scores[i])}"
                results.add(DetectionResult(boundingBox, text))
            }
        }
        return results
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(results: List<DetectionResult>)
    }
}