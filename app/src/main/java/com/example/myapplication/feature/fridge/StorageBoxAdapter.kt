// src/main/java/com/example/myapplication/feature/fridge/StorageBoxAdapter.kt

package com.example.myapplication.feature.fridge

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemStorageBoxBinding
import com.example.myapplication.feature.fridge.data.Storage
import com.example.myapplication.feature.fridge.data.Ingredient
import com.example.myapplication.R // R.drawable 리소스를 사용하기 위해 필요

/**
 * 냉장고 메인 화면의 저장 공간 목록을 위한 어댑터
 * 각 저장 공간 박스 내부에 재료 아이콘 슬라이더를 포함합니다.
 */
class StorageBoxAdapter(
    private var storages: List<Storage>,
    private var ingredientsByStorage: Map<Long, List<Ingredient>>,
    private val onIngredientClick: (Ingredient) -> Unit,
    // ⭐ 새로 추가: 저장 공간 설정 버튼 클릭 콜백 ⭐
    private val onStorageSettingsClick: (Storage) -> Unit
) : RecyclerView.Adapter<StorageBoxAdapter.StorageBoxViewHolder>() {

    private val ingredientAdapters = mutableMapOf<Long, IngredientIconAdapter>()

    class StorageBoxViewHolder(
        val binding: ItemStorageBoxBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageBoxViewHolder {
        val binding = ItemStorageBoxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StorageBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageBoxViewHolder, position: Int) {
        val storage = storages[position]
        val context = holder.itemView.context

        // 1. 저장 공간 기본 정보 설정
        holder.binding.textStorageName.text = storage.name

        // 2. 저장 공간 아이콘 설정
        // 아이콘 리소스 이름(storage.iconResName)을 기반으로 R.drawable ID를 찾습니다.
        val iconResId = context.resources.getIdentifier(
            storage.iconResName,
            "drawable",
            context.packageName
        )
        // 찾지 못하면 기본 아이콘(ic_fridge)을 사용합니다.
        holder.binding.imageStorageIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_fridge)

        // ⭐ 2. 설정 버튼 클릭 리스너 구현 및 Storage 객체 전달 ⭐
        holder.binding.buttonStorageSettings.setOnClickListener {
            onStorageSettingsClick(storage) // storage는 Storage 객체입니다.
        }

        // ⭐ 3. 설정 버튼 가시성 및 클릭 리스너 통합 (수정된 부분) ⭐
        if (storage.isDefault) {
            // 기본 저장 공간은 버튼 숨김 및 리스너 제거
            holder.binding.buttonStorageSettings.visibility = ViewGroup.GONE
            holder.binding.buttonStorageSettings.setOnClickListener(null)
        } else {
            // 편집 가능한 저장 공간은 버튼 표시 및 리스너 설정
            holder.binding.buttonStorageSettings.visibility = ViewGroup.VISIBLE
            holder.binding.buttonStorageSettings.setOnClickListener {
                onStorageSettingsClick(storage) // 클릭된 Storage 객체를 콜백으로 전달
            }
        }

        // 4. 재료 아이콘 슬라이더 (RecyclerView) 설정 및 연결
        val currentIngredients = ingredientsByStorage[storage.id] ?: emptyList()

        if (ingredientAdapters[storage.id] == null) {
            // ... (어댑터 생성 및 설정 로직 유지) ...
            val adapter = IngredientIconAdapter(currentIngredients, onIngredientClick)
            holder.binding.recyclerIngredientSlider.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            ingredientAdapters[storage.id] = adapter
        } else {
            // 이미 어댑터가 있다면 데이터만 갱신
            ingredientAdapters[storage.id]?.updateIngredients(currentIngredients)
        }

        // 5. 툴팁 로직은 Fragment나 ViewModel에서 처리해야 하므로, 여기서는 클릭 리스너만 정의
    }

    override fun getItemCount(): Int = storages.size

    /**
     * 외부에서 데이터 목록 전체를 갱신할 때 사용합니다.
     */
    fun updateData(newStorages: List<Storage>, newIngredientsByStorage: Map<Long, List<Ingredient>>) {
        storages = newStorages
        ingredientsByStorage = newIngredientsByStorage
        notifyDataSetChanged()
    }
}