package com.example.myapplication.presentation.ui.camera

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: CameraRepository
) : ViewModel() {

    private val _recognizedIngredients = MutableLiveData<List<String>>(emptyList())
    val recognizedIngredients: LiveData<List<String>> get() = _recognizedIngredients

    private var lastAnalyzedTimestamp = 0L

    fun analyzeImage(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 2000) {
            lastAnalyzedTimestamp = currentTimestamp

            // ⭐ 이제 ImageProxy를 직접 전달하므로 오류가 발생하지 않습니다.
            val dummyResult = repository.processImageForIngredients(imageProxy)

            if (dummyResult.isNotEmpty()) {
                val currentList = _recognizedIngredients.value?.toMutableList() ?: mutableListOf()
                dummyResult.forEach {
                    if (!currentList.contains(it)) {
                        currentList.add(it)
                    }
                }
                _recognizedIngredients.postValue(currentList)
            }
        }
    }
}