package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// ⭐ 해결 1: 올바른 Ingredient 모델 클래스를 import 합니다. ⭐
import com.example.myapplication.domain.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IngredientEditViewModel @Inject constructor() : ViewModel() {

    // ⭐ 해결 2: LiveData 타입을 List로 일치시키고, 내부적으로만 MutableList를 사용합니다. ⭐
    // 외부 공개용 (읽기 전용)
    private val _recognizedIngredients = MutableLiveData<List<Ingredient>>(emptyList())
    val recognizedIngredients: LiveData<List<Ingredient>> get() = _recognizedIngredients

    // 화면 회전 등 상태 복원용
    private val _manualIngredients = MutableLiveData<List<Ingredient>>(emptyList())
    val manualIngredients: LiveData<List<Ingredient>> get() = _manualIngredients

    // 편집 대상 재료
    private val _ingredientToEdit = MutableLiveData<Ingredient?>()
    val ingredientToEdit: LiveData<Ingredient?> get() = _ingredientToEdit


    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }

    fun addRecognizedIngredient(ingredient: Ingredient) {
        // 기존 리스트를 가져와서 새 재료를 추가한 후 LiveData에 다시 설정
        val currentList = _recognizedIngredients.value?.toMutableList() ?: mutableListOf()
        currentList.add(ingredient)
        _recognizedIngredients.value = currentList
    }

    fun saveManualIngredients(ingredients: List<Ingredient>) {
        _manualIngredients.value = ingredients
    }

    fun clearManualIngredients() {
        _manualIngredients.value = emptyList()
    }

    fun clearRecognizedIngredients() {
        _recognizedIngredients.value = emptyList()
    }

    fun clearIngredients() {
        clearRecognizedIngredients()
        clearManualIngredients()
    }
}