package com.example.myapplication.presentation.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.SearchRequest
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.RecipeRepository // ✅ RecipeRepository import 추가
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepository,
    private val recipeRepository: RecipeRepository // ✅ RecipeRepository 주입
) : ViewModel() {

    val ingredients: LiveData<List<Ingredient>> = fridgeRepository.getAllIngredients().asLiveData()

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> get() = _storages

    private val _ingredientToEdit = MutableLiveData<Ingredient?>()
    val ingredientToEdit: LiveData<Ingredient?> get() = _ingredientToEdit

    init {
        loadStorages()
    }

    // ✅ Task 2 이동: 유통기한 임박 재료 LiveData 추가 (2일 이내)
    // fridgeRepository.getImminentExpiryIngredients(2)는 D-0 ~ D-2를 필터링합니다.
    val imminentExpiryIngredients: LiveData<List<Ingredient>> =
        fridgeRepository.getImminentExpiryIngredients(2).asLiveData()

    fun loadStorages() {
        viewModelScope.launch {
            _storages.value = fridgeRepository.getAllStorages()
        }
    }

    fun addIngredients(ingredients: List<Ingredient>) {
        viewModelScope.launch {
            // Repository의 addIngredients가 서버 저장 및 로컬 저장을 모두 처리합니다.
            fridgeRepository.addIngredients(ingredients)
            // 재료 추가 후, 추천 레시피 목록을 갱신합니다.
            syncRecipesWithServer()
        }
    }

    fun deleteIngredient(id: Long) {
        viewModelScope.launch {
            // 1. 로컬 DB에서 삭제
            fridgeRepository.deleteIngredient(id)
            // 2. 서버에서도 삭제
            fridgeRepository.deleteIngredientFromServer(id)
            // 3. 레시피 추천 목록 동기화
            syncRecipesWithServer()
        }
    }

    fun updateIngredients(ingredientsToUpdate: List<Ingredient>) {
        viewModelScope.launch {
            ingredientsToUpdate.forEach { fridgeRepository.updateIngredient(it) }
            syncRecipesWithServer() // ✅ 재료 수정 후 동기화
        }
    }

    // ✅ 재료 변경 시 서버에 추천 레시피를 요청하여 로컬 DB를 갱신하는 함수
    private fun syncRecipesWithServer() {
        viewModelScope.launch {
            // 현재 DB에 저장된 모든 재료 목록을 가져옵니다.
            val currentIngredients = fridgeRepository.getAllIngredients().first().map { it.name }
            if (currentIngredients.isNotEmpty()) {
                // 서버에 추천을 요청하고, 결과를 로컬 DB에 덮어씁니다.
                recipeRepository.searchAndCacheRecommendedDishes(
                    SearchRequest(ingredients = currentIngredients)
                )
            }
        }
    }

    // ... (나머지 Storage 및 ingredientToEdit 관련 코드는 동일)
    fun addStorage(name: String, iconResName: String) {
        fridgeRepository.addStorage(name, iconResName)
        loadStorages()
    }

    fun updateStorage(id: Long, newName: String, newIconResName: String) {
        fridgeRepository.updateStorage(id, newName, newIconResName)
        loadStorages()
    }

    fun deleteStorage(id: Long) {
        fridgeRepository.deleteStorage(id)
        loadStorages()
    }

    fun selectIngredientForEdit(ingredient: Ingredient) {
        _ingredientToEdit.value = ingredient
    }

    fun clearIngredientToEdit() {
        _ingredientToEdit.value = null
    }
}