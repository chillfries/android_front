package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.model.RecipeDetailEntity
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.domain.model.Ingredient
@Database(
    entities = [
        Ingredient::class,
        RecommendedDish::class,
        RecipeDetailEntity::class
    ],
    version = 3, // 스키마 변경으로 버전 업
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    RecipeIdConverter::class,
    IngredientDetailConverter::class // ✅ 누락되었던 Converter 추가
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fridgeDao(): FridgeDao
}