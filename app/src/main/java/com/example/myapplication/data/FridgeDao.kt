package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.data.model.RecipeDetailEntity
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeDao {
    // ... (기존 getAllIngredients, addIngredients, updateIngredient, deleteIngredient 함수)

    @Query("DELETE FROM ingredients")
    suspend fun clearAllIngredients() // ✅ 로그인 시 동기화를 위해 추가

    // ... (나머지 함수들은 모두 동일)
    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIngredients(ingredientList: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredient(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecommendedDishes(dishes: List<RecommendedDish>)

    @Query("SELECT * FROM recommended_dishes")
    fun getRecommendedDishes(): Flow<List<RecommendedDish>>

    @Query("DELETE FROM recommended_dishes")
    suspend fun clearRecommendedDishes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeDetailEntity>)

    @Query("SELECT * FROM recipe_details WHERE id IN (:recipeIds)")
    suspend fun getRecipesByIds(recipeIds: List<Int>): List<RecipeDetailEntity>

    @Query("UPDATE recipe_details SET bookmark = :isBookmarked, lastAccessed = :timestamp WHERE id = :recipeId")
    suspend fun updateBookmark(recipeId: Int, isBookmarked: Boolean, timestamp: Long)

    @Query("UPDATE recipe_details SET lastAccessed = :timestamp WHERE id IN (:recipeIds)")
    suspend fun updateLastAccessed(recipeIds: List<Int>, timestamp: Long)

    @Query("DELETE FROM recipe_details WHERE id IN (SELECT id FROM recipe_details WHERE bookmark = 0 ORDER BY lastAccessed ASC LIMIT :count)")
    suspend fun evictOldestRecipes(count: Int)

    @Query("SELECT COUNT(*) FROM recipe_details")
    suspend fun getRecipeCount(): Int
}