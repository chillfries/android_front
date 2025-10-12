package com.example.myapplication.domain.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeRepository {

    // --- Ingredient 관련 ---
    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIngredients(ingredientList: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredient(id: Long)

    // --- Storage 관련 (임시 메모리 방식 유지) ---
    // Room은 복잡한 객체(Storage)를 직접 관리하기보다,
    // 간단한 데이터 타입을 관리하는 데 더 적합합니다.
    // Storage는 추후 서버 연동 시 API로 관리할 예정이므로 지금은 수정하지 않습니다.
    fun getAllStorages(): List<Storage>
    fun addStorage(name: String, iconResName: String)
    fun updateStorage(id: Long, newName: String, newIconResName: String)
    fun deleteStorage(id: Long)
}