package com.example.myapplication.data.repository

import com.example.myapplication.data.FridgeDao
import com.example.myapplication.data.model.*
import com.example.myapplication.domain.repository.RecipeRepository
import com.example.myapplication.network.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.data.model.RecommendedDish


@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val dao: FridgeDao
) : RecipeRepository {

    private val RECIPE_CACHE_LIMIT = 200

    // 재료 변경 시 서버에 요청하고 결과를 DB에 저장
    override suspend fun searchAndCacheRecommendedDishes(searchRequest: SearchRequest): Response<GroupedSearchResponse> {
        val response = apiService.searchRecipes(searchRequest)
        if (response.isSuccessful) {
            response.body()?.results?.map {
                RecommendedDish(it.dishId, it.dishName, it.recipeIds)
            }?.let {
                dao.clearRecommendedDishes()
                dao.insertOrUpdateRecommendedDishes(it)
            }
        }
        return response

    }

    // 레시피 상세 정보 (캐시 우선 조회)
    override fun getRecipeDetails(recipeIds: List<Int>): Flow<Result<List<RecipeDetailEntity>>> = flow {
        try {
            // 1. 로컬 캐시에서 먼저 조회
            val cachedRecipes = dao.getRecipesByIds(recipeIds)
            if (cachedRecipes.isNotEmpty()) {
                // 접근 시간 갱신
                dao.updateLastAccessed(cachedRecipes.map { it.id }, System.currentTimeMillis())
                emit(Result.success(cachedRecipes))
            }

            val cachedIds = cachedRecipes.map { it.id }
            val missingIds = recipeIds.filter { it !in cachedIds }

            // 2. 캐시에 없는 ID만 서버에 요청
            if (missingIds.isNotEmpty()) {
                val response = apiService.getRecipesByIds(missingIds)
                if (response.isSuccessful && response.body() != null) {
                    val newRecipes = response.body()!!.map { it.toEntity() }

                    // 3. 캐시 용량 관리 (LRU)
                    val currentCount = dao.getRecipeCount()
                    val newCount = newRecipes.size
                    if (currentCount + newCount > RECIPE_CACHE_LIMIT) {
                        val evictCount = (currentCount + newCount) - RECIPE_CACHE_LIMIT
                        dao.evictOldestRecipes(evictCount)
                    }

                    // 4. 새로 받은 레시피 저장
                    dao.insertRecipes(newRecipes)

                    // 5. 전체 목록 다시 DB에서 조회하여 방출
                    emit(Result.success(dao.getRecipesByIds(recipeIds)))
                } else {
                    throw Exception("API Error: ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // 북마크 상태 업데이트
    override suspend fun updateBookmark(recipeId: Int, isBookmarked: Boolean) {
        dao.updateBookmark(recipeId, isBookmarked, System.currentTimeMillis())
    }
}