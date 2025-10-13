package com.example.myapplication.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.data.model.SearchRequest
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.domain.repository.RecipeRepository
import com.example.myapplication.domain.model.Ingredient // ⭐ Ingredient import 추가
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val fridgeRepository: FridgeRepository
) : ViewModel() {

    // 기존 추천 목록 LiveData를 내부적으로만 사용
    private val _recommendedDishes = fridgeRepository.getRecommendedDishes().asLiveData()

    // UI에서 최종적으로 바인딩할 데이터 (추천 + 검색 통합)
    private val _dishesForDisplay = MutableLiveData<List<RecommendedDish>>()
    val dishesForDisplay: LiveData<List<RecommendedDish>> get() = _dishesForDisplay

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        // 초기 로딩: 60% 기준으로 추천 목록 로드
        syncRecipes(ratio = 0.6f)
    }

    // ⭐ Task 1, 4: ratio 매개변수를 추가하여 검색 및 추천을 통합 처리
    fun syncRecipes(query: String? = null, ratio: Float = 0.6f) {
        viewModelScope.launch {
            try {
                // 1. 재료 목록 가져오기
                val ingredients = fridgeRepository.getAllIngredients().first().map { it.name }

                // 재료가 없으면 빈 목록 표시
                if (ingredients.isEmpty()) {
                    _dishesForDisplay.postValue(emptyList())
                    return@launch
                }

                // 2. SearchRequest 생성
                val request = SearchRequest(
                    ingredients = ingredients,
                    q = query,
                    ingRatio = ratio
                )

                // 3. 서버 요청 (결과는 로컬 DB에도 캐시됨)
                val response = recipeRepository.searchAndCacheRecommendedDishes(request)

                if (response.isSuccessful) {
                    response.body()?.results?.let {
                        // 받은 결과를 RecommendedDish로 변환하여 표시
                        _dishesForDisplay.postValue(it.map { dish ->
                            RecommendedDish(dish.dishId, dish.dishName, dish.recipeIds)
                        })
                    }
                } else {
                    _error.postValue("검색 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.postValue("검색 중 오류 발생: ${e.message}")
            }
        }
    }
}