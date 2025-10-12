package com.example.myapplication.domain.repository

import android.net.Uri
// ⭐ ImageProxy import 추가
import androidx.camera.core.ImageProxy

interface CameraRepository {
    fun saveCapturedImage(fileUri: Uri): Uri
    // ⭐ 해결: 파라미터 타입을 Uri에서 ImageProxy로 변경
    fun processImageForIngredients(imageProxy: ImageProxy): List<String>
}