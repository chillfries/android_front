package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IngredientEditViewModel @Inject constructor() : ViewModel() {

    private val _recognizedIngredients = MutableLiveData<List<String>>(emptyList())
    val recognizedIngredients: LiveData<List<String>> get() = _recognizedIngredients

    // ⭐ 8. 화면을 벗어나도 유지될 임시 재료 목록
    private val _manualIngredients = MutableLiveData<List<Ingredient>>(emptyList())
    val manualIngredients: LiveData<List<Ingredient>> get() = _manualIngredients

    fun addRecognizedIngredients(ingredients: List<String>) {
        _recognizedIngredients.value = ingredients
    }

    // ⭐ 8. 현재 폼 상태를 ViewModel에 저장하는 함수
    fun saveManualIngredients(ingredients: List<Ingredient>) {
        _manualIngredients.value = ingredients
    }

    fun clearRecognizedIngredients() {
        _recognizedIngredients.value = emptyList()
    }

    // ⭐ 8. 등록 완료 또는 완전 이탈 시 임시 데이터를 초기화하는 함수
    fun clearAllTemporaryIngredients() {
        _recognizedIngredients.value = emptyList()
        _manualIngredients.value = emptyList()
    }
}