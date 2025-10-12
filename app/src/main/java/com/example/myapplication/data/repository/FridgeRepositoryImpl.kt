package com.example.myapplication.data.repository

import com.example.myapplication.data.FridgeDao
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeRepositoryImpl @Inject constructor(
    private val fridgeDao: FridgeDao // Room DAO를 주입받습니다.
) : FridgeRepository {

    // --- Storage 관련 데이터 (계속 임시 메모리 방식 사용) ---
    private val storages = mutableListOf<Storage>()
    private val storageIdCounter = AtomicLong(0)

    init {
        // 앱 시작 시 기본 저장 공간 생성
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "정리되지 않은 재료", iconResName = "ic_unorganized_box", isDefault = true)
        )
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "냉장고 칸", iconResName = "ic_fridge", isDefault = false)
        )
    }

    // --- Ingredient 관련 (이제 Room DB를 사용합니다) ---
    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return fridgeDao.getAllIngredients()
    }

    override suspend fun addIngredient(ingredient: Ingredient) {
        fridgeDao.addIngredient(ingredient)
    }

    override suspend fun addIngredients(ingredientList: List<Ingredient>) {
        fridgeDao.addIngredients(ingredientList)
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        fridgeDao.updateIngredient(ingredient)
    }

    override suspend fun deleteIngredient(id: Long) {
        fridgeDao.deleteIngredient(id)
    }


    // --- Storage 관련 구현 (수정 없음) ---
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
            storages.removeIf { it.id == id }
        }
    }
}