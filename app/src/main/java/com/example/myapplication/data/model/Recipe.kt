// 파일: mmain/main/java/com/example/myapplication/data/model/Recipe.kt

package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

// 서버로 보낼 검색 요청 모델
data class SearchRequest(
    val ingredients: List<String>, // 로컬 DB의 재료 목록
    val q: String? = null,         // 사용자 검색 키워드
    val size: Int = 20,
    val topk: Int = 3,
    @SerializedName("ing_mode") val ingMode: String = "RATIO",
    @SerializedName("ing_ratio") val ingRatio: Float = 0.6f
)

// 서버 응답의 개별 Dish 그룹 모델
data class GroupedDishSearchResult(
    @SerializedName("dish_id") val dishId: Int,
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("recipe_ids") val recipeIds: List<Int> // 해당 Dish의 상위 레시피 ID 목록
)

// 서버 응답 전체 모델
data class GroupedSearchResponse(
    val total: Int,
    val results: List<GroupedDishSearchResult>
)