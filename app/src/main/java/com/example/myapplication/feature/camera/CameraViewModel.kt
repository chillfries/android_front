package com.example.myapplication.feature.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.feature.camera.data.CameraRepository
import android.net.Uri

class CameraViewModel(
    private val repository: CameraRepository = CameraRepository()
) : ViewModel() {

    // ViewModel이 외부에서 사용할 수 있도록 캡슐화된 LiveData를 정의합니다.
    private val _processedIngredients = MutableLiveData<List<String>>()
    val processedIngredients: LiveData<List<String>> get() = _processedIngredients

    /**
     * 촬영 버튼 클릭 시 호출되어 이미지를 처리하는 핵심 비즈니스 로직입니다.
     */
    fun onCaptureCompleted(imageUri: Uri) {
        // 1. 이미지 저장 (로직은 Repository에 위임)
        val savedUri = repository.saveCapturedImage(imageUri)

        // 2. OCR 처리 (로직은 Repository에 위임)
        val ingredients = repository.processImageForIngredients(savedUri)

        // 3. 결과를 LiveData에 반영하여 Fragment에 알림
        _processedIngredients.value = ingredients
    }

    // Fragment가 촬영 시작이나 UI 업데이트에 필요한 상태를 이곳에 LiveData로 정의할 수 있습니다.
}