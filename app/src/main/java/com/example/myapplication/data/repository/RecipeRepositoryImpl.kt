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
import com.example.myapplication.data.model.RecipeIngredient
import com.example.myapplication.data.model.RecipeStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val dao: FridgeDao
) : RecipeRepository {

    private val RECIPE_CACHE_LIMIT = 200

    override suspend fun searchAndCacheRecommendedDishes(searchRequest: SearchRequest): Response<GroupedSearchResponse> {
        val response = apiService.searchRecipes(searchRequest)
        if (response.isSuccessful) {
            val searchResults = response.body()?.results ?: return response
            val userIngredients = searchRequest.ingredients.toSet()

            // 1. 추천된 레시피들의 상세 정보를 가져오기 위해 ID 목록을 추출합니다. (각 요리별 첫 번째 레시피)
            val recipeIdsToFetch = searchResults.mapNotNull { it.recipeIds.firstOrNull() }

            if (recipeIdsToFetch.isNotEmpty()) {
                // 2. 서버에 레시피 상세 정보들을 한 번에 요청합니다.
                val detailsResponse = apiService.getRecipesByIds(recipeIdsToFetch)
                if (detailsResponse.isSuccessful) {
                    val recipeDetailsMap = detailsResponse.body()?.associateBy { it.id } ?: emptyMap()

                    // 3. 서버 응답과 사용자 재료를 비교하여 RecommendedDish 목록을 만듭니다.
                    val recommendedDishes = searchResults.map { searchResult ->
                        val mainRecipeId = searchResult.recipeIds.firstOrNull()
                        val recipeDetail = mainRecipeId?.let { recipeDetailsMap[it] }

                        val requiredIngredients = recipeDetail?.ingredients?.map { detail ->
                            RecipeIngredient(
                                name = detail.ingredient.name,
                                quantity = detail.quantityDisplay ?: "",
                                isSatisfied = userIngredients.contains(detail.ingredient.name)
                            )
                        } ?: emptyList()

                        val satisfiedCount = requiredIngredients.count { it.isSatisfied }

                        val recipeSteps = recipeDetail?.instructions?.let { jsonString ->
                            try {
                                val stepList: List<String> = Gson().fromJson(jsonString, object : TypeToken<List<String>>() {}.type)
                                stepList.mapIndexed { index, description -> RecipeStep(step = index + 1, description = description) }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        } ?: emptyList()


                        RecommendedDish(
                            dishId = searchResult.dishId,
                            dishName = searchResult.dishName,
                            recipeIds = searchResult.recipeIds,
                            satisfiedIngredientCount = satisfiedCount, // 실제 충족 개수로 설정
                            category = "한식", // TODO: 실제 카테고리 데이터로 변경 필요
                            youtubeThumbnailUrl = recipeDetail?.thumbnailUrl ?: "",
                            youtubeUrl = recipeDetail?.youtubeUrl ?: "",
                            requiredIngredients = requiredIngredients,
                            recipeSteps = recipeSteps
                        )
                    }
                    // 4. 로컬 DB에 결과 캐시
                    dao.clearRecommendedDishes()
                    dao.insertOrUpdateRecommendedDishes(recommendedDishes)
                }
            } else {
                // 검색 결과가 없으면 캐시를 비웁니다.
                dao.clearRecommendedDishes()
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