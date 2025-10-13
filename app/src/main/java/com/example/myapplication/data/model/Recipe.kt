package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// --- 서버 통신용 DTOs ---

data class SearchRequest(
    val ingredients: List<String>,
    val q: String? = null,
    val size: Int = 20,
    val topk: Int = 3,
    @SerializedName("ing_mode") val ingMode: String = "RATIO",
    @SerializedName("ing_ratio") val ingRatio: Float = 0.6f
)

data class GroupedDishSearchResult(
    @SerializedName("dish_id") val dishId: Int,
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("recipe_ids") val recipeIds: List<Int>
)

data class GroupedSearchResponse(
    val total: Int,
    val results: List<GroupedDishSearchResult>
)

// ✅ 재료를 서버에 생성할 때 보낼 데이터 형식
data class IngredientCreateRequest(
    val name: String
)

// ✅ 서버에서 재료 정보를 받아올 때의 데이터 형식
data class IngredientResponse(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String
)



// 레시피 상세 정보 DTO (API 응답용)
data class RecipeDetail(
    val id: Int,
    val title: String?,
    val difficulty: Int?,
    @SerializedName("serving_size") val servingSize: String?,
    @SerializedName("cooking_time") val cookingTime: Int?,
    val instructions: String,
    @SerializedName("youtube_url") val youtubeUrl: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    val ingredients: List<RecipeIngredientDetail>
)

data class RecipeIngredientDetail(
    @SerializedName("quantity_display") val quantityDisplay: String?,
    val ingredient: IngredientName
)

data class IngredientName(
    val name: String
)

// --- 데이터베이스 저장용 Entities ---

@Entity(tableName = "recommended_dishes")
data class RecommendedDish(
    @PrimaryKey
    val dishId: Int,
    val dishName: String,
    val recipeIds: List<Int>
)

@Entity(tableName = "recipe_details")
data class RecipeDetailEntity(
    @PrimaryKey
    val id: Int,
    val json: String, // 레시피 상세 정보 전체를 JSON 문자열로 저장
    var bookmark: Boolean = false,
    var lastAccessed: Long = System.currentTimeMillis()
)

// --- 확장 함수 (DTO -> Entity 변환) ---

fun RecipeDetail.toEntity(): RecipeDetailEntity {
    return RecipeDetailEntity(
        id = this.id,
        json = Gson().toJson(this) // RecipeDetail 객체를 JSON 문자열로 변환
    )
}

// --- Type Converters ---

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