// 파일: mmain/main/java/com/example/myapplication/domain/repository/RecipeRepository.kt

package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.SearchRequest
import retrofit2.Response

interface RecipeRepository {
    suspend fun searchRecipes(searchRequest: SearchRequest): Response<GroupedSearchResponse>

    // TODO: 향후 레시피 상세 정보 (by-ids) 조회 기능 추가 예정
}