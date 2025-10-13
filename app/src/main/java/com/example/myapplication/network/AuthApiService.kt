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

    // --- Ingredients ---
    @GET("/api/v1/ingredients/")
    suspend fun getUserIngredients(): Response<List<IngredientResponse>>

    @POST("/api/v1/ingredients/")
    suspend fun addUserIngredient(@Body ingredient: IngredientCreateRequest): Response<IngredientResponse>

    // ✅ 재료 삭제 API 함수 추가
    @DELETE("/api/v1/ingredients/{ingredient_id}")
    suspend fun deleteUserIngredient(@Path("ingredient_id") ingredientId: Long): Response<Unit>
}