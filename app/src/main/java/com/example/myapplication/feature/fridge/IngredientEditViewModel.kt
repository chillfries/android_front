// src/main/java/com/example/myapplication/feature/fridge/IngredientEditViewModel.kt

package com.example.myapplication.feature.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.feature.fridge.data.Ingredient

class IngredientEditViewModel : ViewModel() {

    // 1. 카메라 등으로 인식된 재료 목록 (프래그먼트가 관찰)
    private val _recognizedIngredients = MutableLiveData<MutableList<Ingredient>>(mutableListOf())
    val recognizedIngredients: LiveData<MutableList<Ingredient>> get() = _recognizedIngredients

    // ⭐ 2. NEW: 사용자가 수동으로 입력 중인 재료 목록 상태 (Fragment 재생성 시 복원용) ⭐
    private val _manualIngredients = MutableLiveData<MutableList<Ingredient>>(mutableListOf())
    val manualIngredients: LiveData<MutableList<Ingredient>> get() = _manualIngredients

    // ⭐ 새로 추가: 편집할 재료를 저장하는 LiveData (FridgeViewModel에서 가져옴) ⭐
    private val _ingredientToEdit = MutableLiveData<Ingredient?>()
    val ingredientToEdit: LiveData<Ingredient?> get() = _ingredientToEdit

    /**
     * ⭐ 새로 추가: FridgeFragment에서 편집 대상 재료를 설정할 때 사용 ⭐
     */
    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    /**
     * ⭐ 새로 추가: 편집 완료 또는 취소 후 상태 초기화 ⭐
     */
    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }

    /**
     * 재료 목록에 새로운 재료를 추가합니다. (주로 카메라 인식 결과)
     */
    fun addRecognizedIngredient(ingredient: Ingredient) {
        val currentList = _recognizedIngredients.value ?: mutableListOf()
        // TODO: (개선) 재료가 이미 목록에 있으면 수량만 증가시키는 로직 추가
        currentList.add(ingredient)
        _recognizedIngredients.value = currentList
    }

    /**
     * ⭐ NEW: 현재 화면의 모든 재료 폼 상태를 ViewModel에 저장합니다. ⭐
     * Fragment의 onPause 시점에 호출되어 데이터가 유지되도록 합니다.
     */
    fun saveManualIngredients(ingredients: List<Ingredient>) {
        // 복원 시 사용될 수 있도록 mutable list로 복사하여 저장합니다.
        _manualIngredients.value = ingredients.toMutableList()
    }

    /**
     * ⭐ NEW: 수동 입력 목록을 초기화합니다. (Fragment 복원 후 또는 등록 완료 시 호출) ⭐
     */
    fun clearManualIngredients() {
        _manualIngredients.value = mutableListOf()
    }

    /**
     * ⭐ 추가: 인식된 재료 목록을 초기화합니다. (Fragment 복원 후 또는 등록 완료 시 호출) ⭐
     */
    fun clearRecognizedIngredients() {
        _recognizedIngredients.value = mutableListOf()
    }

    /**
     * 재료 목록을 초기화합니다 (등록 완료 후 호출).
     */
    fun clearIngredients() {
        // ⭐ 수정: 새로 추가된 clearRecognizedIngredients 호출 ⭐
        clearRecognizedIngredients()
        // 수동 입력 목록도 함께 초기화합니다.
        clearManualIngredients()
    }
}