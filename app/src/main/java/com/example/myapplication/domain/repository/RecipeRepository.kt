package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.RecipeDetailEntity
import com.example.myapplication.data.model.SearchRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface RecipeRepository {
    suspend fun searchAndCacheRecommendedDishes(searchRequest: SearchRequest): Response<GroupedSearchResponse>
    fun getRecipeDetails(recipeIds: List<Int>): Flow<Result<List<RecipeDetailEntity>>>
    suspend fun updateBookmark(recipeId: Int, isBookmarked: Boolean)
}