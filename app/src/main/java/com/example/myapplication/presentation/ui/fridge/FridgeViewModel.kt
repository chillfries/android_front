package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepository
) : ViewModel() {

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> get() = _ingredients

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> get() = _storages

    private val _ingredientToEdit = MutableLiveData<Ingredient?>()
    val ingredientToEdit: LiveData<Ingredient?> get() = _ingredientToEdit

    init {
        loadData()
    }

    fun loadData() {
        _ingredients.value = fridgeRepository.getAllIngredients()
        _storages.value = fridgeRepository.getAllStorages()
    }

    fun addIngredients(ingredients: List<Ingredient>) {
        fridgeRepository.addIngredients(ingredients)
        loadData()
    }

    // 단일 재료 저장/수정
    fun saveIngredient(ingredient: Ingredient) {
        if (ingredient.id != 0L) {
            fridgeRepository.updateIngredient(ingredient)
        } else {
            fridgeRepository.addIngredient(ingredient)
        }
        loadData()
    }

    // ⭐ 해결: 여러 재료를 한 번에 업데이트하는 함수 추가
    fun updateIngredients(ingredientsToUpdate: List<Ingredient>) {
        ingredientsToUpdate.forEach { fridgeRepository.updateIngredient(it) }
        loadData()
    }

    fun addStorage(name: String, iconResName: String) {
        fridgeRepository.addStorage(name, iconResName)
        loadData()
    }

    fun updateStorage(id: Long, newName: String, newIconResName: String) {
        fridgeRepository.updateStorage(id, newName, newIconResName)
        loadData()
    }

    fun deleteStorage(id: Long) {
        fridgeRepository.deleteStorage(id)
        loadData()
    }

    fun deleteIngredient(id: Long) {
        fridgeRepository.deleteIngredient(id)
        loadData()
    }

    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }
}