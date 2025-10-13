package com.example.myapplication.presentation.ui.home

import androidx.lifecycle.*
import com.example.myapplication.data.model.RecipeDetailEntity
import com.example.myapplication.domain.repository.RecipeRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipeIds = MutableLiveData<List<Int>>()

    // recipeIds가 변경되면 repository에 상세 정보를 요청
    val recipeDetails: LiveData<Result<List<RecipeDetailEntity>>> = _recipeIds.switchMap { ids ->
        recipeRepository.getRecipeDetails(ids).asLiveData()
    }

    fun loadRecipes(ids: List<Int>) {
        _recipeIds.value = ids
    }

    fun toggleBookmark(recipeId: Int) {
        viewModelScope.launch {
            val currentRecipe = recipeDetails.value?.getOrNull()?.find { it.id == recipeId }
            currentRecipe?.let {
                recipeRepository.updateBookmark(it.id, !it.bookmark)
            }
        }
    }
}