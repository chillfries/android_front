package com.example.myapplication.data.repository

import com.example.myapplication.data.FridgeDao
import com.example.myapplication.data.model.IngredientCreateRequest
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.network.AuthApiService
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.data.model.UserIngredientsCreate
import com.example.myapplication.data.model.UserIngredientCreate
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import kotlinx.coroutines.flow.map // ⭐ map import 추가
import java.time.temporal.ChronoUnit // ⭐ java.time 관련 import 추가
import java.time.LocalDate
import java.time.ZoneId

@Singleton
class FridgeRepositoryImpl @Inject constructor(
    private val fridgeDao: FridgeDao,
    private val apiService: AuthApiService
) : FridgeRepository {

    // ... (storages 관련 코드는 동일) ...

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

    override suspend fun addIngredients(ingredientList: List<Ingredient>) {
        try {
            // 1. 서버에 전송할 요청 데이터를 만듭니다.
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val request = UserIngredientsCreate(
                ingredients = ingredientList.map {
                    UserIngredientCreate(
                        ingredientName = it.name,
                        expirationDate = sdf.format(it.expiryDate)
                    )
                }
            )

            // 2. 서버 API를 호출합니다.
            val response = apiService.addUserIngredients(request)

            // 3. API 호출이 성공하면, 서버로부터 받은 응답 데이터를 처리합니다.
            if (response.isSuccessful) {
                val serverIngredients = response.body() ?: emptyList()

                // 4. 서버 응답을 로컬 DB에 저장할 Ingredient 객체 형태로 변환합니다.
                val localIngredients = serverIngredients.map { userIngredient ->
                    val expiryDate = try {
                        sdf.parse(userIngredient.expirationDate) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    Ingredient(
                        id = userIngredient.id.toLong(), // ✨ 서버가 생성한 ID를 사용합니다.
                        name = userIngredient.ingredient.name,
                        // 서버 응답에는 수량, 단위, 위치 정보가 없으므로 기본값을 사용합니다.
                        quantity = 1,
                        unit = "개",
                        storageLocation = "정리되지 않은 재료",
                        expiryDate = expiryDate
                    )
                }

                // 5. 서버 ID가 적용된 재료 목록을 로컬 DB에 저장합니다.
                fridgeDao.addIngredients(localIngredients)

            } else {
                // API 호출 실패 시 에러 로그를 남깁니다.
                Log.e("FridgeRepository", "Failed to add ingredients: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace() // 네트워크 오류 등 예외 처리
        }
    }

    override suspend fun updateIngredient(ingredient: Ingredient) { // ✅ domain.model.Ingredient 참조
        fridgeDao.updateIngredient(ingredient)
    }
    // ✅ 재료를 서버에 추가하는 함수 구현
    override suspend fun addIngredientsToServer(ingredients: List<Ingredient>) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val request = UserIngredientsCreate(
                ingredients = ingredients.map {
                    UserIngredientCreate(
                        ingredientName = it.name,
                        expirationDate = sdf.format(it.expiryDate)
                    )
                }
            )
            apiService.addUserIngredients(request)
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
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val localIngredients = serverIngredients.map { userIngredient ->
                    val expiryDate = try {
                        sdf.parse(userIngredient.expirationDate) ?: Date()
                    } catch (e: Exception) {
                        Date() // 파싱 실패 시 오늘 날짜
                    }

                    Ingredient(
                        id = userIngredient.id.toLong(), // ✅ 서버의 user_ingredient 테이블의 id를 그대로 사용
                        name = userIngredient.ingredient.name,
                        quantity = 1, // 서버에는 수량 정보가 없으므로 기본값 사용
                        unit = "개",  // 서버에는 단위 정보가 없으므로 기본값 사용
                        storageLocation = "정리되지 않은 재료", // 동기화 시 기본값으로 설정
                        expiryDate = expiryDate
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