package com.example.myapplication.presentation.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentCameraBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        observeViewModel()
    }

    private fun checkPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    initializeDetectorAndCamera()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(requireContext(), "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("앱을 사용하려면 카메라 권한을 허용해야 합니다.\n[설정] > [권한]에서 권한을 허용해주세요.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

    private fun initializeDetectorAndCamera() {
        objectDetectorHelper = ObjectDetectorHelper(requireContext(), this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.previewView.post {
            startCamera()
        }
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
            .setTargetResolution(Size(binding.previewView.width, binding.previewView.height))
            .setTargetRotation(binding.previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888) // toBitmap을 위해 포맷 변경
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
            cameraProvider.bindToLifecycle(this.viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("CameraFragment", "카메라 바인딩 실패", e)
        }
    }

    private fun observeViewModel() {
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { ingredients ->
            ingredients?.let {
                // ✅ 오류가 해결된 내비게이션 코드
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
            if (isAdded) { // Fragment가 Activity에 붙어있을 때만 UI 업데이트
                binding.overlayView.setResults(
                    results,
                    binding.previewView.width,
                    binding.previewView.height
                )
                val detectedIngredients = results.map { it.text.split(",")[0] }.distinct()
                // 인식된 재료가 있고, 아직 다음 화면으로 이동하지 않았을 때만 호출
                if (detectedIngredients.isNotEmpty() && viewModel.navigateToEdit.value == null) {
                    viewModel.onIngredientsDetected(detectedIngredients)
                }
            }
        }
    }

    // YUV 이미지를 Bitmap으로 변환하는 함수
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
        _binding = null // 메모리 누수 방지
    }
}