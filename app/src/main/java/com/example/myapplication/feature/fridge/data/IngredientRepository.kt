// src/main/java/com/example/myapplication/feature/fridge/data/IngredientRepository.kt

package com.example.myapplication.feature.fridge.data

import android.util.Log
import java.util.Date

// (Ingredient 모델은 Ingredient.kt 파일에 정의되어 있다고 가정)
// Ingredient 모델의 생성자 순서가 (..., unit: String, storageLocation: String, expiryDate: Date)라고 가정하고 수정합니다.

// 재료 데이터 접근을 담당 (현재는 메모리 리스트 사용)
class IngredientRepository {

    // ⭐ 테스트를 위한 초기 데이터 설정 수정 ⭐
    private val ingredientList = mutableListOf<Ingredient>(
        // Date()와 "과일칸"의 위치를 바꿔 String, Date 순서로 맞춥니다.
        Ingredient(1L, "사과", 3, "개", "과일칸", Date()),
        Ingredient(2L, "우유", 1, "팩", "냉장", Date(System.currentTimeMillis() + 86400000 * 5)),
        Ingredient(3L, "양파", 5, "개", "정리되지 않은 재료", Date(System.currentTimeMillis() - 86400000 * 2))
    )
    private var nextId = 4L

    fun addIngredient(ingredient: Ingredient): Boolean {
        if (ingredient.name.isBlank()) {
            Log.e("Repository", "재료 이름은 필수입니다.")
            return false
        }
        // ID가 0L인 경우에만 새로 할당합니다.
        val ingredientWithId = if (ingredient.id == 0L) ingredient.copy(id = nextId++) else ingredient.copy()
        ingredientList.add(ingredientWithId)
        Log.d("Repository", "재료 추가됨: ${ingredientWithId.name} (ID: ${ingredientWithId.id})")
        return true
    }

    /**
     * ⭐ 추가: 재료 수정 (ID가 일치하는 재료를 찾아 업데이트) ⭐
     */
    fun updateIngredient(ingredient: Ingredient): Boolean {
        val index = ingredientList.indexOfFirst { it.id == ingredient.id }
        if (index != -1) {
            ingredientList[index] = ingredient // 해당 인덱스에 재료 객체 통째로 덮어쓰기
            Log.d("Repository", "재료 수정됨: ${ingredient.name} (ID: ${ingredient.id})")
            return true
        }
        Log.e("Repository", "수정할 재료 (ID: ${ingredient.id})를 찾을 수 없습니다.")
        return false
    }

    fun getAllIngredients(): List<Ingredient> {
        return ingredientList.toList()
    }

    /**
     * ⭐ 추가: 저장 공간 삭제 시 해당 공간의 재료를 기본 공간으로 이동 ⭐
     */
    fun moveIngredientsToDefaultStorage(deletedStorageName: String, defaultStorageName: String): Int {
        var movedCount = 0
        ingredientList.forEachIndexed { index, ingredient ->
            if (ingredient.storageLocation == deletedStorageName) {
                // 재료 객체를 복사하여 storageLocation을 변경
                ingredientList[index] = ingredient.copy(storageLocation = defaultStorageName)
                movedCount++
            }
        }
        return movedCount
    }

    /**
     * ⭐ 추가: 재료 삭제 ⭐
     */
    fun deleteIngredient(id: Long): Boolean {
        return ingredientList.removeIf { it.id == id }.also {
            if (it) Log.d("Repository", "재료 삭제됨 (ID: $id)")
        }
    }
}