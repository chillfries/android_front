package com.example.myapplication.data

import androidx.room.TypeConverter
import com.example.myapplication.data.model.RecipeIngredientDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import com.example.myapplication.data.model.RecipeIngredient // ⭐ 추가
import com.example.myapplication.data.model.RecipeStep // ⭐ 추가

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

// ⭐ KSP 오류 해결을 위한 새 TypeConverter: List<RecipeIngredient> 용
class RecipeIngredientListConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromListToString(ingredients: List<RecipeIngredient>): String {
        return gson.toJson(ingredients)
    }

    @TypeConverter
    fun fromStringToList(json: String): List<RecipeIngredient> {
        val listType = object : TypeToken<List<RecipeIngredient>>() {}.type
        return gson.fromJson(json, listType)
    }
}

// ⭐ KSP 오류 해결을 위한 새 TypeConverter: List<RecipeStep> 용
class RecipeStepListConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromListToString(steps: List<RecipeStep>): String {
        return gson.toJson(steps)
    }

    @TypeConverter
    fun fromStringToList(json: String): List<RecipeStep> {
        val listType = object : TypeToken<List<RecipeStep>>() {}.type
        return gson.fromJson(json, listType)
    }
}