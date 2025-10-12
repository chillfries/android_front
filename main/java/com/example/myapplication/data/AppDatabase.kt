package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.repository.FridgeRepository

@Database(entities = [Ingredient::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fridgeDao(): FridgeRepository // DAO 인터페이스를 반환하는 추상 메서드
}