package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.FridgeRepository
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

class FridgeRepositoryImpl : FridgeRepository {

    // --- Storage 임시 데이터 ---
    private val storages = mutableListOf<Storage>()
    private val storageIdCounter = AtomicLong(0)

    // --- Ingredient 임시 데이터 ---
    private val ingredients = mutableListOf<Ingredient>()
    private val ingredientIdCounter = AtomicLong(0)

    init {
        // Storage 초기 데이터
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "정리되지 않은 재료", iconResName = "ic_unorganized_box", isDefault = true)
        )
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "냉장고 칸", iconResName = "ic_fridge", isDefault = false)
        )

        // Ingredient 초기 데이터
        ingredients.add(
            Ingredient(id = ingredientIdCounter.incrementAndGet(), name = "사과", quantity = 3, unit = "개", storageLocation = "냉장고 칸", expiryDate = Date())
        )
        ingredients.add(
            Ingredient(id = ingredientIdCounter.incrementAndGet(), name = "우유", quantity = 1, unit = "팩", storageLocation = "냉장고 칸", expiryDate = Date(System.currentTimeMillis() + 86400000 * 5))
        )
        ingredients.add(
            Ingredient(id = ingredientIdCounter.incrementAndGet(), name = "양파", quantity = 5, unit = "개", storageLocation = "정리되지 않은 재료", expiryDate = Date(System.currentTimeMillis() - 86400000 * 2))
        )
    }

    // --- Storage 관련 구현 ---
    override fun getAllStorages(): List<Storage> = storages.toList()

    override fun addStorage(name: String, iconResName: String) {
        val newStorage = Storage(
            id = storageIdCounter.incrementAndGet(),
            name = name,
            iconResName = iconResName,
            isDefault = false
        )
        storages.add(newStorage)
    }

    override fun updateStorage(id: Long, newName: String, newIconResName: String) {
        val index = storages.indexOfFirst { it.id == id }
        if (index != -1) {
            storages[index] = storages[index].copy(name = newName, iconResName = newIconResName)
        }
    }

    override fun deleteStorage(id: Long) {
        if (id == 1L) return // 기본 저장 공간 삭제 방지
        val storageToDelete = storages.find { it.id == id }
        if (storageToDelete != null) {
            // 해당 저장 공간의 재료들을 기본 저장 공간으로 이동
            val defaultStorageName = storages.first { it.isDefault }.name
            ingredients.forEachIndexed { index, ingredient ->
                if (ingredient.storageLocation == storageToDelete.name) {
                    ingredients[index] = ingredient.copy(storageLocation = defaultStorageName)
                }
            }
            storages.removeIf { it.id == id }
        }
    }

    // --- Ingredient 관련 구현 ---
    override fun getAllIngredients(): List<Ingredient> = ingredients.toList()

    override fun addIngredient(ingredient: Ingredient) {
        ingredients.add(ingredient.copy(id = ingredientIdCounter.incrementAndGet()))
    }

    override fun addIngredients(ingredientList: List<Ingredient>) {
        ingredientList.forEach { addIngredient(it) }
    }

    override fun updateIngredient(ingredient: Ingredient) {
        val index = ingredients.indexOfFirst { it.id == ingredient.id }
        if (index != -1) {
            ingredients[index] = ingredient
        }
    }

    override fun deleteIngredient(id: Long) {
        ingredients.removeIf { it.id == id }
    }
}