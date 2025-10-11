package com.example.myapplication.presentation.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate) {

    private val viewModel: CameraViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 거부되었습니다.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        requestCameraPermission()

        // "다음" 버튼 클릭 시, ViewModel에 저장된 재료 목록을 다음 화면으로 전달
        binding.nextButton.setOnClickListener {
            val ingredients = viewModel.recognizedIngredients.value
            if (ingredients.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "인식된 재료가 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // Fragment Result API를 사용해 이전 화면으로 데이터 전달
                setFragmentResult("camera_result", bundleOf("recognized_ingredients" to ingredients))
                findNavController().popBackStack()
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // ViewModel의 재료 목록 LiveData를 관찰하여 UI 업데이트
        viewModel.recognizedIngredients.observe(viewLifecycleOwner) { ingredients ->
            binding.recognitionCountText.text = "인식된 재료: ${ingredients.size} 개"
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview UseCase 설정 (화면에 미리보기를 보여줌)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // ImageAnalysis UseCase 설정 (실시간 프레임 분석)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        // ViewModel에 이미지 분석 요청 (현재는 더미 데이터)
                        viewModel.analyzeImage(imageProxy)
                        imageProxy.close() // 분석 후 반드시 닫아주어야 함
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                // ImageCapture 대신 imageAnalyzer를 바인딩
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}