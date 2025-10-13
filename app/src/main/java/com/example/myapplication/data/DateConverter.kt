package com.example.myapplication.data

import androidx.room.TypeConverter
import com.example.myapplication.data.model.RecipeIngredientDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class RecipeIdConverter {
    @TypeConverter
    fun fromListIntToString(recipeIds: List<Int>): String {
        return recipeIds.joinToString(",")
    }

    @TypeConverter
    fun fromStringToListInt(data: String): List<Int> {
        if (data.isEmpty()) return emptyList()
        return data.split(',').map { it.toInt() }
    }
}

class IngredientDetailConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromListToString(ingredients: List<RecipeIngredientDetail>): String {
        return gson.toJson(ingredients)
    }

    @TypeConverter
    fun fromStringToList(json: String): List<RecipeIngredientDetail> {
        val listType = object : TypeToken<List<RecipeIngredientDetail>>() {}.type
        return gson.fromJson(json, listType)
    }
}