package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.myapplication.domain.model.Ingredient
@HiltViewModel
class IngredientEditViewModel @Inject constructor() : ViewModel() {

    private val _recognizedIngredients = MutableLiveData<List<String>>(emptyList())
    val recognizedIngredients: LiveData<List<String>> get() = _recognizedIngredients

    private val _manualIngredients = MutableLiveData<List<Ingredient>>(emptyList())
    val manualIngredients: LiveData<List<Ingredient>> get() = _manualIngredients

    fun addRecognizedIngredients(ingredients: List<String>) {
        _recognizedIngredients.value = ingredients
    }

    fun saveManualIngredients(ingredients: List<Ingredient>) {
        _manualIngredients.value = ingredients
    }

    fun clearRecognizedIngredients() {
        _recognizedIngredients.value = emptyList()
    }

    // ⭐ 해결: 함수 이름을 'clearAllIngredients'로 변경
    fun clearAllIngredients() {
        _recognizedIngredients.value = emptyList()
        _manualIngredients.value = emptyList()
    }

    fun clearAllForms() {
        _manualIngredients.value = emptyList()
        _recognizedIngredients.value = emptyList()
    }
}