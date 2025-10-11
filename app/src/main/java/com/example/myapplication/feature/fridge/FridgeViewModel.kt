// src/main/java/com/example/myapplication/feature/fridge/FridgeViewModel.kt

package com.example.myapplication.feature.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.feature.fridge.data.Ingredient
import com.example.myapplication.feature.fridge.data.IngredientRepository
import com.example.myapplication.feature.fridge.data.Storage
import com.example.myapplication.feature.fridge.data.StorageRepository // ⭐ 필요 ⭐


class FridgeViewModel(
    private val ingredientRepository: IngredientRepository = IngredientRepository(),
    private val storageRepository: StorageRepository = StorageRepository() // ⭐ StorageRepository는 정의되어 있다고 가정합니다. ⭐
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
        _ingredients.value = ingredientRepository.getAllIngredients()
        _storages.value = storageRepository.getAllStorages()
    }

    /**
     * ⭐ 추가: 다수의 재료를 한 번에 추가합니다. (IngredientEditFragment에서 사용) ⭐
     */
    fun addIngredients(ingredients: List<Ingredient>) {
        ingredients.forEach {
            ingredientRepository.addIngredient(it)
        }
        loadData()
    }

    /**
     * ⭐ 추가: 단일 재료를 저장/수정합니다. (IngredientEditFragment에서 사용) ⭐
     */
    fun saveIngredient(ingredient: Ingredient) {
        if (ingredient.id != 0L) {
            ingredientRepository.updateIngredient(ingredient)
        } else {
            ingredientRepository.addIngredient(ingredient)
        }
        loadData()
    }

    /**
     * 저장 공간을 추가하고 목록을 갱신합니다.
     */
    fun addStorage(name: String, iconResName: String) {
        storageRepository.addStorage(name, iconResName)
        loadData()
    }

    /**
     * ⭐ 추가: 저장 공간 수정 (EditStorageBottomSheet에서 사용) ⭐
     */
    fun updateStorage(id: Long, newName: String, newIconResName: String) {
        storageRepository.updateStorage(id, newName, newIconResName)
        loadData()
    }

    /**
     * 저장 공간을 삭제하고 목록을 갱신합니다.
     */
    fun deleteStorage(id: Long) {
        // StorageRepository에 deleteStorage(id) 함수가 정의되어 있어야 합니다.
        storageRepository.deleteStorage(id)
        loadData()
    }

    /**
     * 편집할 재료를 설정하고, 편집 페이지로 이동할 준비를 마칩니다.
     */
    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    /**
     * 편집 페이지 이동 후 상태를 초기화합니다.
     */
    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }
}