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
    version = 4, // ⭐ 버전 4로 증가 (스키마 변경 알림)
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    RecipeIdConverter::class,
    IngredientDetailConverter::class,
    RecipeIngredientListConverter::class, // ⭐ 추가 등록
    RecipeStepListConverter::class       // ⭐ 추가 등록
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fridgeDao(): FridgeDao
}