package com.example.myapplication.network

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface AuthApiService {
    // --- User Auth ---
    @POST("/api/v1/users/signup")
    suspend fun signup(@Body user: UserCreateRequest): Response<Unit>

    @POST("/api/v1/users/login")
    suspend fun login(@Body user: UserLoginRequest): Response<LoginResponse>

    // --- Recipes ---
    @POST("/api/v1/dishes/search/grouped")
    suspend fun searchRecipes(@Body searchRequest: SearchRequest): Response<GroupedSearchResponse>

    @POST("/api/v1/recipes/by-ids")
    suspend fun getRecipesByIds(@Body recipeIds: List<Int>): Response<List<RecipeDetail>>

    // --- Ingredients --

    // --- Ingredients ---
    @GET("/api/v1/ingredients/me") // ✅ 로그인 시 동기화를 위한 API
    suspend fun getUserIngredients(): Response<List<UserIngredientResponse>>

    @POST("/api/v1/ingredients/me") // ✅ 재료 추가 시 ID를 포함한 응답을 받음
    suspend fun addUserIngredients(@Body ingredients: UserIngredientsCreate): Response<List<UserIngredientResponse>>

    // ✅ 재료 삭제 API 함수 (기존 코드 유지)
    @DELETE("/api/v1/ingredients/{ingredient_id}")
    suspend fun deleteUserIngredient(@Path("ingredient_id") ingredientId: Long): Response<Unit>
}