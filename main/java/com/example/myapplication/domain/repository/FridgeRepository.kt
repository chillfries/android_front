package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage

interface FridgeRepository {
    // Storage
    fun getAllStorages(): List<Storage>
    fun addStorage(name: String, iconResName: String)
    fun updateStorage(id: Long, newName: String, newIconResName: String)
    fun deleteStorage(id: Long)

    // Ingredient
    fun getAllIngredients(): List<Ingredient>
    fun addIngredient(ingredient: Ingredient)
    fun addIngredients(ingredientList: List<Ingredient>)
    fun updateIngredient(ingredient: Ingredient)
    fun deleteIngredient(id: Long)
}