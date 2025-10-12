// 파일: mmain/main/java/com/example/myapplication/presentation/ui/home/HomeViewModel.kt

package com.example.myapplication.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.SearchRequest
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val fridgeRepository: FridgeRepository // 로컬 재료를 가져오기 위해 필요
) : ViewModel() {

    private val _searchResult = MutableLiveData<GroupedSearchResponse>()
    val searchResult: LiveData<GroupedSearchResponse> = _searchResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // D-Day 대시보드에서 사용할 LiveData (Room에서 가져온 재료 목록)
    val allIngredientsFlow = fridgeRepository.getAllIngredients()

    fun searchRecipes(query: String?) {
        viewModelScope.launch {
            try {
                // 1. 로컬 DB에서 현재 보유 재료 목록을 가져옴
                val ingredientsList = allIngredientsFlow.first().map { it.name }

                // 2. 검색 요청 객체 생성 (재료 기반 추천 + 키워드 검색)
                val request = SearchRequest(
                    ingredients = ingredientsList,
                    q = query
                )

                // 3. 서버 API 호출
                val response = recipeRepository.searchRecipes(request)

                if (response.isSuccessful && response.body() != null) {
                    _searchResult.postValue(response.body())
                } else {
                    _error.postValue("레시피 검색 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.postValue("네트워크 오류: ${e.message}")
            }
        }
    }
}