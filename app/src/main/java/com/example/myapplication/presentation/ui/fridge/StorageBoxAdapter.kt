package com.example.myapplication.presentation.ui.fridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemStorageBoxBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage

class StorageBoxAdapter(
    private var storages: List<Storage>,
    private var ingredientsByStorage: Map<Long, List<Ingredient>>,
    private val onIngredientClick: (Ingredient) -> Unit,
    private val onStorageBoxClick: (Storage) -> Unit, // ⭐ 저장 공간 박스 전체 클릭 리스너
    private val onStorageSettingsClick: (Storage) -> Unit
) : RecyclerView.Adapter<StorageBoxAdapter.StorageBoxViewHolder>() {

    // 각 저장 공간의 재료 슬라이더(RecyclerView)는 각자의 어댑터를 가집니다.
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

        holder.binding.textStorageName.text = storage.name

        // 아이콘 리소스 이름으로 실제 Drawable ID를 찾아서 설정
        val iconResId = context.resources.getIdentifier(
            storage.iconResName,
            "drawable",
            context.packageName
        )
        holder.binding.imageStorageIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_fridge)

        // "정리되지 않은 재료" 같은 기본 저장 공간은 설정 버튼을 숨김
        if (storage.isDefault) {
            holder.binding.buttonStorageSettings.visibility = View.GONE
            holder.binding.buttonStorageSettings.setOnClickListener(null)
        } else {
            holder.binding.buttonStorageSettings.visibility = View.VISIBLE
            holder.binding.buttonStorageSettings.setOnClickListener {
                onStorageSettingsClick(storage)
            }
        }

        // ⭐ 저장 공간 박스 전체에 대한 클릭 리스너 설정
        holder.binding.root.setOnClickListener {
            onStorageBoxClick(storage)
        }

        // 현재 저장 공간에 해당하는 재료 목록 가져오기
        val currentIngredients = ingredientsByStorage[storage.id] ?: emptyList()

        // 재료 슬라이더(내부 RecyclerView) 설정
        if (ingredientAdapters[storage.id] == null) {
            // 어댑터가 없으면 새로 생성
            val adapter = IngredientIconAdapter(currentIngredients, onIngredientClick)
            holder.binding.recyclerIngredientSlider.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            ingredientAdapters[storage.id] = adapter
        } else {
            // 어댑터가 이미 있으면 데이터만 업데이트
            ingredientAdapters[storage.id]?.updateIngredients(currentIngredients)
        }
    }

    override fun getItemCount(): Int = storages.size

    fun updateData(newStorages: List<Storage>, newIngredientsByStorage: Map<Long, List<Ingredient>>) {
        storages = newStorages
        ingredientsByStorage = newIngredientsByStorage
        notifyDataSetChanged() // 데이터가 바뀌었음을 알리고 UI를 새로 그림
    }
}