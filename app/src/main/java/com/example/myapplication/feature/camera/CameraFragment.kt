package com.example.myapplication.feature.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.net.Uri
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.feature.camera.CameraViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val viewModel: CameraViewModel by viewModels()
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService // java.util.concurrent.ExecutorService 대신 간략화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fragment 인스턴스 생성 시 Executor 초기화
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⭐ 1. LiveData 관찰 및 UI 업데이트 ⭐
        viewModel.processedIngredients.observe(viewLifecycleOwner) { ingredients ->
            binding.recognitionCountText.text = "인식된 재료: ${ingredients.size} 개"

            if (ingredients.isNotEmpty()) {
                // Toast.makeText(requireContext(), "재료 ${ingredients.size}개 인식 완료", Toast.LENGTH_SHORT).show()
                // 이 시점에서는 다음 버튼을 활성화하거나 색상을 변경하는 등의 UI 처리를 수행할 수 있습니다.
            }
        }

        // ⭐ 2. '다음' 버튼 리스너 수정 (captureButton -> nextButton) ⭐
        binding.nextButton.setOnClickListener {
            // TODO: 실제 구현 시에는 PreviewView에서 최종 촬영 결과 URI를 얻는 로직이 들어갑니다.
            val tempUri = Uri.parse("file:///temp/image_${System.currentTimeMillis()}.jpg")

            // 핵심: 촬영된 URI를 ViewModel에게 넘겨 비즈니스 로직(저장, OCR)을 실행하도록 위임
            viewModel.onCaptureCompleted(tempUri)

            // OCR 처리 후 결과 관찰 (위의 LiveData 관찰 로직)을 통해 화면 전환이 일어납니다.
        }

        // ⭐ 3. 뒤로가기 버튼 리스너 추가 ⭐
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // ⭐ onDestroyView: View Binding만 해제 ⭐
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ⭐ onDestroy: Fragment 인스턴스가 완전히 파괴될 때 Executor 해제 ⭐
    override fun onDestroy() {
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        super.onDestroy()
    }
}