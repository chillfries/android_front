package com.example.myapplication.data.repository

import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.myapplication.domain.repository.CameraRepository
import javax.inject.Inject // ✅ Inject import 추가

// ✅ @Inject constructor() 추가
class CameraRepositoryImpl @Inject constructor() : CameraRepository {
    override fun saveCapturedImage(fileUri: Uri): Uri {
        Log.d("CameraRepository", "이미지 저장 로직 실행: $fileUri")
        return fileUri
    }

    override fun processImageForIngredients(imageProxy: ImageProxy): List<String> {
        // TODO: ML Kit 등 실제 OCR 라이브러리를 사용하여 imageProxy에서 텍스트를 추출하는 로직 구현
        Log.d("CameraRepository", "실시간 이미지 프레임 분석 로직 실행")
        // 현재는 더미 데이터를 반환합니다.
        return listOf("인식된 사과", "인식된 우유")
    }
}