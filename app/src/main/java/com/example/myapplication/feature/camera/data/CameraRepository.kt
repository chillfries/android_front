package com.example.myapplication.feature.camera.data

import android.net.Uri
import android.util.Log

// 카메라/이미지 관련 데이터 처리 로직을 담당합니다.
class CameraRepository {

    /**
     * 촬영된 이미지를 내부 저장소에 저장하고 URI를 반환하는 로직입니다.
     */
    fun saveCapturedImage(fileUri: Uri): Uri {
        // TODO: 실제 이미지 파일 저장 및 처리 로직 (이후 MediaStore나 File API 사용)
        Log.d("CameraRepository", "이미지 저장 로직 실행: $fileUri")
        return fileUri
    }

    /**
     * 이미지에서 재료 정보를 추출하는 OCR 로직입니다.
     */
    fun processImageForIngredients(imageUri: Uri): List<String> {
        // TODO: ML Kit, Tesseract 등 OCR 모델을 사용하는 로직이 들어갈 위치
        Log.d("CameraRepository", "OCR 처리 로직 실행: $imageUri")
        return listOf("인식된 사과", "인식된 우유")
    }
}