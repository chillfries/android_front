package com.example.myapplication.data.repository

import com.example.myapplication.data.FridgeDao
import com.example.myapplication.data.model.IngredientCreateRequest
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.network.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // ⭐ map import 추가
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit // ⭐ java.time 관련 import 추가

@Singleton
class FridgeRepositoryImpl @Inject constructor(
    private val fridgeDao: FridgeDao,
    private val apiService: AuthApiService
) : FridgeRepository {

    // Date를 LocalDate로 변환하는 헬퍼 함수
    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    // ⭐ Task 2: 임박 재료 목록을 필터링하는 함수 구현
    override fun getImminentExpiryIngredients(days: Int): Flow<List<Ingredient>> {
        return fridgeDao.getAllIngredients().map { ingredients ->
            val today = LocalDate.now()
            ingredients.filter {
                val expiryLocalDate = it.expiryDate.toLocalDate()
                val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryLocalDate).toInt()
                // D-day (0)부터 D-days(포함) 이내의 재료만 필터링 (유통기한이 지난 것은 제외)
                daysUntilExpiry >= 0 && daysUntilExpiry <= days
            }.sortedBy { it.expiryDate } // 유통기한이 가까운 순으로 정렬
        }
    }

    override fun getAllIngredients(): Flow<List<Ingredient>> { // ✅ domain.model.Ingredient 참조
        return fridgeDao.getAllIngredients()
    }

    override fun getRecommendedDishes(): Flow<List<RecommendedDish>> {
        return fridgeDao.getRecommendedDishes()
    }

    override suspend fun addIngredients(ingredientList: List<Ingredient>) { // ✅ domain.model.Ingredient 참조
        fridgeDao.addIngredients(ingredientList)
    }

    override suspend fun updateIngredient(ingredient: Ingredient) { // ✅ domain.model.Ingredient 참조
        fridgeDao.updateIngredient(ingredient)
    }
    // ✅ 재료를 서버에 추가하는 함수 구현
    override suspend fun addIngredientToServer(ingredientName: String) {
        try {
            apiService.addUserIngredient(IngredientCreateRequest(name = ingredientName))
        } catch (e: Exception) {
            e.printStackTrace() // TODO: 예외 처리
        }
    }

    // ✅ 로그인 시 재료 동기화 함수 구현
    override suspend fun syncIngredientsOnLogin() {
        try {
            val response = apiService.getUserIngredients()
            if (response.isSuccessful) {
                val serverIngredients = response.body() ?: emptyList()
                val localIngredients = serverIngredients.map {
                    Ingredient(
                        // 서버에는 id만 있고 나머지 필드가 없으므로 기본값으로 채웁니다.
                        id = it.id.toLong(),
                        name = it.name,
                        quantity = 1,
                        unit = "개",
                        storageLocation = "정리되지 않은 재료",
                        expiryDate = Date() // 오늘 날짜
                    )
                }
                fridgeDao.clearAllIngredients()
                fridgeDao.addIngredients(localIngredients)
            }
        } catch (e: Exception) {
            e.printStackTrace() // TODO: 예외 처리
        }
    }
    // ... (나머지 코드는 동일) ...
    private val storages = mutableListOf<Storage>()
    private val storageIdCounter = AtomicLong(0)

    init {
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "정리되지 않은 재료", iconResName = "ic_unorganized_box", isDefault = true)
        )
        storages.add(
            Storage(id = storageIdCounter.incrementAndGet(), name = "냉장고 칸", iconResName = "ic_fridge", isDefault = false)
        )
    }
    override suspend fun deleteIngredient(id: Long) {
        fridgeDao.deleteIngredient(id)
    }

    override fun getAllStorages(): List<Storage> = storages.toList()

    override fun addStorage(name: String, iconResName: String) {
        val newStorage = Storage(id = storageIdCounter.incrementAndGet(), name = name, iconResName = iconResName)
        storages.add(newStorage)
    }

    override fun updateStorage(id: Long, newName: String, newIconResName: String) {
        val index = storages.indexOfFirst { it.id == id }
        if (index != -1) {
            storages[index] = storages[index].copy(name = newName, iconResName = newIconResName)
        }
    }

    override fun deleteStorage(id: Long) {
        if (storages.any { it.id == id && it.isDefault }) return
        storages.removeIf { it.id == id }
    }

    override suspend fun deleteIngredientFromServer(id: Long) {
        try {
            apiService.deleteUserIngredient(ingredientId = id)
        } catch (e: Exception) {
            e.printStackTrace() // TODO: 예외 처리
        }
    }
}