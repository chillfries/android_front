package com.example.myapplication.presentation.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _navigateToEdit = MutableLiveData<List<String>?>()
    val navigateToEdit: LiveData<List<String>?> = _navigateToEdit

    // 재료가 인식되면 이 함수를 호출하여 화면 이동을 트리거합니다.
    fun onIngredientsDetected(ingredients: List<String>) {
        if (ingredients.isNotEmpty()) {
            _navigateToEdit.value = ingredients
        }
    }

    // 화면 이동이 완료된 후, LiveData를 초기화하여 중복 이동을 방지합니다.
    fun onNavigationComplete() {
        _navigateToEdit.value = null
    }
}