package com.example.myapplication

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentFridgeBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FridgeFragment : Fragment() {

    private var _binding: FragmentFridgeBinding? = null
    private val binding get() = _binding!!

    // 카메라 실행을 위한 Executor 서비스
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment 진입 시 카메라 권한 요청
        checkCameraPermission()

        // 카메라 Executor 초기화
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun checkCameraPermission() {
        // 1. 권한 리스너 생성
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(requireContext(), "카메라 권한 승인됨.", Toast.LENGTH_SHORT).show()
                // 권한 승인 시 카메라 실행
                startCamera()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(requireContext(), "카메라 권한이 거부되었습니다. 재료 등록이 불가능합니다.", Toast.LENGTH_LONG).show()
            }
        }

        // 2. TedPermission 실행
        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("카메라 권한을 거부하시면 냉장고 재료 등록이 불가능합니다. [설정] > [권한]에서 허용해 주세요.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // CameraProvider를 가져옵니다.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview 객체를 생성합니다.
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // 후면 카메라를 기본으로 설정합니다.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 이전 바인딩을 해제합니다.
                cameraProvider.unbindAll()

                // Preview를 카메라에 바인딩합니다.
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

            } catch(exc: Exception) {
                Log.e("FridgeFragment", "카메라 바인딩에 실패했습니다.", exc)
                Toast.makeText(requireContext(), "카메라 실행에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment가 소멸될 때 카메라 Executor 종료
        cameraExecutor.shutdown()
        _binding = null
    }
}