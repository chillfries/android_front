// src/main/java/com/example/myapplication/feature/fridge/data/StorageRepository.kt

package com.example.myapplication.feature.fridge.data

import android.util.Log
import java.util.concurrent.atomic.AtomicLong // ID 생성을 위해 사용

/**
 * 저장 공간(Storage) 데이터를 관리하는 Repository.
 * (DAO/Database 로직 대신 임시로 인메모리 데이터를 사용합니다.)
 */
class StorageRepository {

    // 임시 인메모리 데이터 저장소
    private val storages = mutableListOf<Storage>()
    private val idCounter = AtomicLong(0)

    // 초기 데이터 (기본 저장 공간) 설정
    init {
        // ID=1L은 '정리되지 않은 재료' 저장 공간으로 가정
        storages.add(
            Storage(
                id = idCounter.incrementAndGet(),
                name = "정리되지 않은 재료",
                iconResName = "ic_fridge",
                isDefault = true
            )
        )
        // 임시 저장 공간 추가
        storages.add(
            Storage(
                id = idCounter.incrementAndGet(),
                name = "냉장고 칸",
                iconResName = "ic_fridge",
                isDefault = false
            )
        )
    }

    // ⭐ 1. FridgeViewModel.loadData()에서 사용됨 ⭐
    fun getAllStorages(): List<Storage> {
        return storages.toList()
    }

    // ⭐ 2. FridgeViewModel.addStorage()에서 사용됨 ⭐
    fun addStorage(name: String, iconResName: String) {
        val newStorage = Storage(
            id = idCounter.incrementAndGet(),
            name = name,
            iconResName = iconResName,
            isDefault = false
        )
        storages.add(newStorage)
        Log.d("StorageRepository", "저장 공간 추가: $name")
    }

    // ⭐ 3. Unresolved reference: editStorage 오류 해결 ⭐
    fun updateStorage(id: Long, newName: String, newIconResName: String) {
        val index = storages.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldStorage = storages[index]
            storages[index] = oldStorage.copy(name = newName, iconResName = newIconResName)
            Log.d("StorageRepository", "저장 공간 편집 완료: ID $id")
        }
    }

    // ⭐ 4. Unresolved reference: deleteStorage 오류 해결 ⭐
    fun deleteStorage(id: Long) {
        // 기본 저장 공간(ID=1L로 가정)은 삭제하지 못하도록 보호
        if (id == 1L) {
            Log.e("StorageRepository", "기본 저장 공간은 삭제할 수 없습니다.")
            return
        }

        // 실제 구현에서는 이 저장 공간에 속한 모든 재료를 '정리되지 않은 재료'로 이동시키는 로직이 추가되어야 합니다.
        // 현재는 해당 로직을 생략하고 저장 공간만 삭제합니다.
        val removed = storages.removeIf { it.id == id }
        if (removed) {
            Log.d("StorageRepository", "저장 공간 삭제 완료: ID $id")
        }
    }
}