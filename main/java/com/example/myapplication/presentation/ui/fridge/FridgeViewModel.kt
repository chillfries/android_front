package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.FridgeRepositoryImpl
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepositoryImpl // 구현체를 직접 주입받습니다.
) : ViewModel() {

    // Flow를 LiveData로 변환하여 DB 변경을 실시간으로 UI에 반영합니다.
    val ingredients: LiveData<List<Ingredient>> = fridgeRepository.getAllIngredients().asLiveData()

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> get() = _storages

    private val _ingredientToEdit = MutableLiveData<Ingredient?>()
    val ingredientToEdit: LiveData<Ingredient?> get() = _ingredientToEdit

    init {
        loadStorages()
    }

    // loadData() 대신 loadStorages()로 변경 (재료는 Flow가 자동으로 로드)
    fun loadStorages() {
        viewModelScope.launch {
            _storages.value = fridgeRepository.getAllStorages()
        }
    }

    // 이제 모든 DB 작업은 코루틴 스코프 내에서 비동기 처리됩니다.
    fun addIngredients(ingredients: List<Ingredient>) {
        viewModelScope.launch {
            fridgeRepository.addIngredients(ingredients)
        }
    }

    fun updateIngredients(ingredientsToUpdate: List<Ingredient>) {
        viewModelScope.launch {
            ingredientsToUpdate.forEach { fridgeRepository.updateIngredient(it) }
        }
    }

    fun deleteIngredient(id: Long) {
        viewModelScope.launch {
            fridgeRepository.deleteIngredient(id)
        }
    }

    fun addStorage(name: String, iconResName: String) {
        fridgeRepository.addStorage(name, iconResName)
        loadStorages() // 저장 공간 목록 새로고침
    }

    fun updateStorage(id: Long, newName: String, newIconResName: String) {
        fridgeRepository.updateStorage(id, newName, newIconResName)
        loadStorages()
    }

    fun deleteStorage(id: Long) {
        fridgeRepository.deleteStorage(id)
        loadStorages()
    }

    // --- UI 상태 관련 함수 (수정 없음) ---
    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }
}