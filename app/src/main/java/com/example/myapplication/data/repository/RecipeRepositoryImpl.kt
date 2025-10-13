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
import com.example.myapplication.data.model.RecipeIngredient // ⭐ 추가
import com.example.myapplication.data.model.RecipeStep // ⭐ 추가

// ⭐ 임시 더미 데이터 정의 (통합 테스트용)
private val DUMMY_INGREDIENTS = listOf(
    RecipeIngredient(name = "돼지고기", quantity = "300g", isSatisfied = true),
    RecipeIngredient(name = "김치", quantity = "1/4 포기", isSatisfied = true),
    RecipeIngredient(name = "양파", quantity = "1/2개", isSatisfied = false),
    RecipeIngredient(name = "두부", quantity = "1/2 모", isSatisfied = false)
)
private val DUMMY_STEPS = listOf(
    RecipeStep(step = 1, description = "김치와 돼지고기를 썰어 냄비에 넣고 볶습니다."),
    RecipeStep(step = 2, description = "물 1컵과 양념을 넣고 끓입니다."),
    RecipeStep(step = 3, description = "두부를 넣고 5분간 더 끓입니다.")
)
// ***************************************************************

// ... (class definition)

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val dao: FridgeDao
) : RecipeRepository {

    private val RECIPE_CACHE_LIMIT = 200

    override suspend fun searchAndCacheRecommendedDishes(searchRequest: SearchRequest): Response<GroupedSearchResponse> {
        val response = apiService.searchRecipes(searchRequest)
        if (response.isSuccessful) {
            response.body()?.results?.map {
                // ⭐ 수정: 새로운 필드에 더미 데이터를 채우도록 변경
                RecommendedDish(
                    dishId = it.dishId,
                    dishName = it.dishName,
                    recipeIds = it.recipeIds,
                    satisfiedIngredientCount = (2..4).random(), // 임시 값
                    category = "한식 / 찌개",
                    youtubeThumbnailUrl = "https://img.youtube.com/vi/dummy_id_${it.dishId}/mqdefault.jpg",
                    youtubeUrl = "https://www.youtube.com/watch?v=dummy_id_${it.dishId}",
                    requiredIngredients = DUMMY_INGREDIENTS,
                    recipeSteps = DUMMY_STEPS
                )
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