package com.example.myapplication.presentation.ui.camera

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ✅ BaseFragment 상속 방식 변경 (오류 해결 1)
@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate),
    ObjectDetectorHelper.DetectorListener {

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        objectDetectorHelper = ObjectDetectorHelper(requireContext(), this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.previewView.post {
            startCamera()
        }
        observeViewModel()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.previewView.display.rotation)
            .build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

        imageAnalyzer = ImageAnalysis.Builder()
            // ✅ targetResolution 대신 setTargetResolution 사용 (오류 해결 2 - 버전 호환성)
            .setTargetResolution(Size(binding.previewView.width, binding.previewView.height))
            .setTargetRotation(binding.previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val bitmap = toBitmap(imageProxy)
                    if (bitmap != null) {
                        objectDetectorHelper.detect(bitmap, imageProxy.imageInfo.rotationDegrees)
                    }
                    imageProxy.close()
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("CameraFragment", "카메라 바인딩 실패", e)
        }
    }

    private fun observeViewModel() {
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { ingredients ->
            ingredients?.let {
                val action = CameraFragmentDirections.actionCameraFragmentToIngredientListEditFragment(it.toTypedArray())
                findNavController().navigate(action)
                viewModel.onNavigationComplete()
            }
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread { Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show() }
    }

    override fun onResults(results: List<DetectionResult>) {
        activity?.runOnUiThread {
            // ✅ targetResolution 대신 previewView의 크기를 사용 (오류 해결 3)
            binding.overlayView.setResults(
                results,
                binding.previewView.width,
                binding.previewView.height
            )
            val detectedIngredients = results.map { it.text.split(",")[0] }.distinct()
            if (detectedIngredients.isNotEmpty()) {
                viewModel.onIngredientsDetected(detectedIngredients)
                imageAnalyzer?.clearAnalyzer()
            }
        }
    }

    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {
        val planes = imageProxy.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        objectDetectorHelper.close()
    }
}