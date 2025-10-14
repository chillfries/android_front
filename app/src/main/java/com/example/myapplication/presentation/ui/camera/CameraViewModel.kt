package com.example.myapplication.presentation.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    // 인식된 재료 목록을 기반으로 화면 이동을 트리거하는 LiveData
    private val _navigateToEdit = MutableLiveData<List<String>?>()
    val navigateToEdit: LiveData<List<String>?> get() = _navigateToEdit

    // 재료가 인식되었을 때 호출되는 함수
    fun onIngredientsDetected(ingredients: List<String>) {
        _navigateToEdit.value = ingredients
    }

    // 화면 이동이 완료된 후 LiveData를 초기화하는 함수
    fun onNavigationComplete() {
        _navigateToEdit.value = null
    }
}