package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import kotlinx.coroutines.flow.Flow

// @Dao 어노테이션 제거! 이제 순수한 인터페이스입니다.
interface FridgeRepository {

    // --- Ingredient 관련 ---
    fun getAllIngredients(): Flow<List<Ingredient>>
    suspend fun addIngredient(ingredient: Ingredient)
    suspend fun addIngredients(ingredientList: List<Ingredient>)
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(id: Long)

    // --- Storage 관련 ---
    fun getAllStorages(): List<Storage>
    fun addStorage(name: String, iconResName: String)
    fun updateStorage(id: Long, newName: String, newIconResName: String)
    fun deleteStorage(id: Long)
}