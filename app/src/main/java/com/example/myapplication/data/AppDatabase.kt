// 파일: mmain/main/java/com/example/myapplication/data/AppDatabase.kt

package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.domain.model.Ingredient

// ✅ entities에 Ingredient::class를 포함하고, version을 1로 설정합니다.
@Database(entities = [Ingredient::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // FridgeRepository 대신 새로 만든 FridgeDao를 반환하도록 수정
    abstract fun fridgeDao(): FridgeDao
}