// 파일: mmain/main/java/com/example/myapplication/data/repository/RecipeRepositoryImpl.kt

package com.example.myapplication.data.repository

import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.SearchRequest
import com.example.myapplication.domain.repository.RecipeRepository
import com.example.myapplication.network.AuthApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
) : RecipeRepository {
    override suspend fun searchRecipes(searchRequest: SearchRequest): Response<GroupedSearchResponse> {
        return apiService.searchRecipes(searchRequest)
    }
}