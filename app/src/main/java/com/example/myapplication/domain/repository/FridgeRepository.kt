package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.domain.model.Ingredient // ✅ import 경로 확인
import com.example.myapplication.domain.model.Storage
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun getAllIngredients(): Flow<List<Ingredient>> // ✅ domain.model.Ingredient 참조
    suspend fun addIngredients(ingredientList: List<Ingredient>) // ✅ domain.model.Ingredient 참조
    suspend fun updateIngredient(ingredient: Ingredient) // ✅ domain.model.Ingredient 참조
    suspend fun deleteIngredient(id: Long)
    fun getRecommendedDishes(): Flow<List<RecommendedDish>>
    fun getAllStorages(): List<Storage>
    fun addStorage(name: String, iconResName: String)
    fun updateStorage(id: Long, newName: String, newIconResName: String)
    fun deleteStorage(id: Long)
    suspend fun addIngredientToServer(ingredientName: String)
    suspend fun syncIngredientsOnLogin()
    suspend fun deleteIngredientFromServer(id: Long)
    // ⭐ Task 2: 유통기한 임박 재료를 가져오는 함수 추가
    fun getImminentExpiryIngredients(days: Int): Flow<List<Ingredient>>
}